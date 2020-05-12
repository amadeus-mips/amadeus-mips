// See README.md for license details.

package cpu.cache

import chisel3._
import cpu.common.NiseSramWriteIO
import shared.{CircularShifter, CircularShifterInt, PseudoLRUMRU, PseudoLRUTree, TrueLRU}
//import chisel3.util.{log2Ceil, Cat}
import chisel3.util._
import cpu.common.NiseSramReadIO
import cpu.common.DefaultConfig._
import cpu.performance.CachePerformanceMonitorIO
import shared.AXIIO
import shared.Constants._

//TODO: discuss propagating the signal
//TODO: change into a read only interface
//TODO: be able to invalidate the refill buffer
//TODO: optimize axi port

//TODO: better software engineering with d-cache
/**
  * DCache with an AXI interface
  * @param setAmount how many sets there are in the d-cache
  * @param wayAmount how many ways there are in each set of d-cache
  * @param bankAmount how many banks there are in the d-cache
  * @param performanceMonitorEnable whether to enable the performance metrics
  */
class newDCache(
  setAmount:                Int = 64,
  wayAmount:                Int = 4,
  bankAmount:               Int = 16,
  performanceMonitorEnable: Boolean = false
) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val rData = Flipped(new NiseSramReadIO)
    val wData = Flipped(new NiseSramWriteIO)
    //TODO: customize performance IO for D-cache
    val performanceMonitorIO = if (performanceMonitorEnable) Some(new CachePerformanceMonitorIO) else None
  })

  /**
    * |   tag     |   index     |   bankOffset      | 0.U(2.W)  |
    * | `tagLen`  | `indexLen`  | `log2(bankAmount)`|     2     |
    */
  //----------------------------------------------------------------
  //------------------set up the cache parameters-------------------
  //----------------------------------------------------------------
  val indexLen = log2Ceil(setAmount) // index宽度
  val bankSize = 32 / 8 // 每bank字节数
  val blockSize = bankAmount * bankSize // 每块字节数
  val tagLen = 32 - indexLen - log2Ceil(blockSize) // tag宽度

  //------------------------------------------------------------------------------------
  //------------------check if the generator parameters meeet requirements--------------
  //------------------------------------------------------------------------------------
  require(bankAmount <= 16 && bankAmount >= 1, s"bank amount is $bankAmount! Need between 1 and 16")
  require(isPow2(bankAmount), "bank amount should be a power of 2")
  require(isPow2(setAmount), "setAmount of the cache must be a power of 2")
  require(blockSize % 4 == 0, "the block size of the cache ( in number of bytes ) must be 4 aligned")

  //-------------------------------------------------------------------------------
  //--------------------set up the states and register of FSM----------------------
  //-------------------------------------------------------------------------------
  // main finite state machine for handling misses
  // idle is always the hit state, write could only happen during idle as of now
  // when a miss comes and lru is dirty, enters invalidate, if lru not dirty, enter wait for AR
  // after writing back, enters read miss state
  // handle the write miss during sIdle ( after the whole cycle )
  val sIdle :: sInvalidate :: sTransfer :: sWriteFinish :: sWaitForAR :: sReFill :: sWriteBack :: Nil = Enum(7)
  val state = RegInit(sIdle)

  // this is really just an int, tracking which index I'm writing to in
  // the refill buffer, i.e. next target
  val refillWriteMask = Module(new CircularShifterInt(bankAmount))

  // write vec marks which words in the refill buffer has been written to i.e. per word valid
  val refillWriteVec = RegInit(VecInit(Seq.fill(bankAmount)(false.B)))

  // refill buffer is a buffer for holding values from axi r channel
  // at the end of the refill states this buffer will be re-written to
  // the d-cache line
  val reFillBuffer = RegInit(VecInit(Seq.fill(bankAmount)(0.U(32.W))))

  // record the PC info on a miss
  // keep track of which way to write to
  val lruWayReg = Reg(UInt(log2Ceil(wayAmount).W))

  // keep track of the index
  val indexReg = Reg(UInt(indexLen.W))

  // keep track of the tag
  val tagReg = Reg(UInt(tagLen.W))

  // keep track of the specific index of the bank
  val bankOffsetReg = Reg(UInt(log2Ceil(bankAmount).W))

  // Notice: this won't be true across rWriteBack to rIdle, as there is
  // no lookup during rWriteBack
  // hit in the reFill buffer during refill stage
  val rDataReg = RegNext(io.axi.r.bits.data)
  val rValidReg = RegInit(false.B)

  // hit under miss in d-cache, set this register
  // hit in the D-cache during reFill stage
  val rDCacheHitReg = RegInit(false.B)

  // records if the current set is full
  // this is to make sure empty lines are filled ahead of LRU
  val isSetNotFull = WireDefault(false.B)
  val emptyPtr = WireDefault(0.U(log2Ceil(wayAmount).W))
  //-----------------------------------------------------------------------------
  //------------------assertions to check--------------------------------------
  //-----------------------------------------------------------------------------
//  assert(!(io.rData.valid && io.rData.addr(1, 0).orR), "when address is not aligned, the valid signal must be false")
  assert(!(io.rData.valid && !io.rData.enable), "the returned data should not be valid when the address is not enabled")
  //-------------------------------------------------------------------------------
  //-------------------- setup some constants to use----------------------------------
  //-------------------------------------------------------------------------------
  val addr = io.rData.addr
  val cachedTrans = addr(31, 29) =/= "b101".U

  val tag = addr(dataLen - 1, dataLen - tagLen)
  val index = addr(dataLen - tagLen - 1, dataLen - tagLen - indexLen)
  val bankOffset = addr(log2Ceil(blockSize) - 1, log2Ceil(bankSize))

  //-------------------------------------------------------------------------------
  //--------------------set up the memory banks and wire them to their states------
  //-------------------------------------------------------------------------------
  /** valid(way)(index) */
  val valid = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(setAmount)(false.B)))))

  /**
    * dirty(way)(index)
    */
  val dirty = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(setAmount)(false.B)))))

  // the dirty status of the set.
  val dirtyWire = Wire(Vec(wayAmount, Bool()))

  /** Write enable mask, we(way)(bank) */
  val we = Wire(Vec(wayAmount, Vec(bankAmount, Bool())))

  /** Tag write enable mask, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))

  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))

  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  // there are several backends for LRU, mru performs better than tree
  val LRU = Module(new PseudoLRUMRU(numOfWay = wayAmount, numOfSets = setAmount))

  val tagWire = Wire(UInt(tagLen.W))

  val indexWire = Wire(UInt(indexLen.W))
  //-----------------------------------------------------------------------------
  //------------------set up variables for this cycle----------------------------
  //-----------------------------------------------------------------------------
  tagWire := tag
  // check what state we are in
  //TODO: notice this does not precisely imply the time, as it state update happens
  // within the cycle
  val isIdle = state === sIdle
  val isWaitForAR = state === sWaitForAR
  val isReFill = state === sReFill
  val isWriteBack = state === sWriteBack

  // check if there is a hit and the line that got the hit if there is a hit
  // despite its name, this indicates a true hit
  val isHit = WireDefault(false.B)

  // check which is the lru line
  val lruLine = LRU.io.lruLine

  // check if there is a hit and determine the way of the hit
  val hitWay = Wire(UInt(log2Ceil(wayAmount).W))
  hitWay := 0.U

  // check across all ways in the desired set
  for (i <- 0 until wayAmount) {
    when(!valid(i)(index)) {
      isSetNotFull := true.B
      emptyPtr := i.U
    }
    when(valid(i)(index) && tagData(i) === tag) {
      hitWay := i.U
      isHit := true.B
    }
    dirtyWire(i) := dirty(i)(index)
  }

  //-----------------------------------------------------------------------------
  //------------------initialize default IO--------------------------------
  //-----------------------------------------------------------------------------

  // by default, don't write back to bank
  we := 0.U.asTypeOf(Vec(wayAmount, Vec(bankAmount, Bool())))
  // by default, don't change the tag
  tagWe := 0.U.asTypeOf(Vec(wayAmount, Bool()))

  // default io for write mask
  refillWriteMask.io.initPosition.bits := 0.U
  refillWriteMask.io.initPosition.valid := false.B
  refillWriteMask.io.shiftEnable := false.B

  io.axi := DontCare

  io.axi.ar.bits.id := DATA_ID
  io.axi.ar.bits.addr := Mux(
    cachedTrans,
    //    Cat(0.U(3.W), addr(28, 2 + log2Ceil(bankAmount)), 0.U((2 + log2Ceil(bankAmount)).W)),
    Cat(0.U(3.W), Mux(state === sIdle, addr(28, 0), Cat(tagReg, indexReg, bankOffsetReg, 0.U(2.W))(28, 0))),
    virToPhy(addr)
  )

  io.axi.ar.bits.len := Mux(cachedTrans, (bankAmount - 1).U(4.W), 0.U(4.W)) // 16 or 1
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b10".U(2.W) // wrap burst

  /**
    * there was a design where if there is a miss, assert valid immediately
    * maybe it is not a good idea, need to know how this translate to verilog
    */
  io.axi.ar.valid := isWaitForAR

  //  io.axi.r.ready := state === sReFill
  io.axi.r.ready := true.B

  // these are hard wired as required by Loongson
  io.axi.ar.bits.lock := 0.U
  //TODO: can we utilize this?
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U
  // hardcode r to always be ready
  //TODO: check in case of problem

  // the contents for a read
  val cacheContents = Wire(UInt(dataLen.W))
  val hitWayReg = RegNext(hitWay)
  val bankOffSetNextReg = RegNext(bankOffset)
  cacheContents := bankData(hitWayReg)(bankOffSetNextReg)
  // read data is only valid when read enable is asserted
  io.rData.valid := cachedTrans && isIdle && isHit && io.rData.enable
  io.rData.data := cacheContents

  LRU.io.accessEnable := false.B
  LRU.io.accessWay := DontCare
  LRU.io.accessSet := index

  //-----------------------------------------------------------------------------
  //------------------transaction as functions-----------------------------------
  //-----------------------------------------------------------------------------
  def beginRTransaction(): Unit = {
    state := sReFill
    refillWriteMask.io.initPosition.valid := true.B
    refillWriteMask.io.initPosition.bits := bankOffsetReg
    // the precise timing of this should happen at the first cycle of the r transaction
    // however, as it is put into the refill state ( because if you wait for R, then
    // you can't handle the first receive unless you put it into a reg, but that's kind
    // of ugly
    refillWriteVec := 0.U.asTypeOf(refillWriteVec)
    reFillBuffer := 0.U.asTypeOf(reFillBuffer)
    // don't invalidate this line
  }

  def beginARTransaction(): Unit = {
    indexReg := index
    //    lruWayReg := lruLine
    tagReg := tag
    bankOffsetReg := bankOffset
  }

  /**
    * this is to check whether there is a hit in the
    * d-cache when the d-cache is under a miss
    * if there is a hit, send the cacheContents ( this cycle )
    * to the rData, valid has been asserted last cycle
    */
  def checkDCacheHit(): Unit = {
    when(rDCacheHitReg) {
      rDCacheHitReg := false.B
      io.rData.data := cacheContents
    }
  }

  /**
    * check if there is a hit in the refill buffer
    * this is to preserve the result across the fsm
    * state boundary
    */
  def checkRefillBufferHit(): Unit = {
    when(rValidReg) {
      rValidReg := false.B
      io.rData.data := rDataReg
    }
  }

  /**
    * update lru to point at the access way
    * @param accessWay the way that I'm going to access
    */
  def updateLRU(accessWay: UInt): Unit = {
    LRU.io.accessEnable := true.B
    LRU.io.accessWay := accessWay
  }
  //-----------------------------------------------------------------------------
  //------------------fsm transformation-----------------------------------------
  //-----------------------------------------------------------------------------
  switch(state) {
    is(sIdle) {

      when(io.rData.enable) {
        // enable already ensures that the address is aligned
        when(isHit) {
          updateLRU(hitWay)
        }.otherwise {
          state := sWaitForAR
          beginARTransaction()

          io.axi.ar.valid := true.B
          when(io.axi.ar.fire) {
            beginRTransaction()
          }
        }
      }
    }
    is(sWaitForAR) {
      when(io.axi.ar.fire) {
        beginRTransaction()
      }
    }
    is(sReFill) {
      assert(io.axi.r.bits.id === INST_ID, "r id is not supposed to be different from d-cache id")
      assert(io.axi.r.bits.resp === 0.U, "the response should always be okay")

      // with every successful transaction, increment the bank offset register to reflect the new value
      when(io.axi.r.fire) {
        // update the write mask
        refillWriteMask.io.shiftEnable := true.B

        // write to the refill buffer instead of the data bank
        reFillBuffer(refillWriteMask.io.vector) := io.axi.r.bits.data
        // update the vec to record which positions has been written to
        refillWriteVec(refillWriteMask.io.vector) := true.B

        // write to each bank consequently
        bankOffsetReg := bankOffsetReg + 1.U

        io.rData.valid := false.B
        // by default, this is connected to the rData register that preserves the hit
        // in refill buffer or axi from the previous cycle, this could also be connected
        // to d-cache ( instruction variable )
        io.rData.data := rDataReg
        // this defaults to false, as this value is only used in the write back state
        rValidReg := false.B
        checkDCacheHit()

        /**
          * check if there is a hit in the d-cache
          */
        when(isHit && io.rData.enable) {
          io.rData.valid := true.B
          rDCacheHitReg := true.B
          updateLRU(hitWay)
        }

        // refill is hit when tag is a hit, and index is a hit, and the data from axi is ready
        // when index and tag are hit
        when((tag === tagReg) && (index === indexReg) && io.rData.enable) {
          // when there is a direct hit from the bank
          when((bankOffset === bankOffsetReg)) {
            io.rData.valid := true.B
            rValidReg := true.B
            // r data reg = axi r bits by default
          }.elsewhen(refillWriteVec(bankOffset)) {
            // if the hit occurs in the refill buffer
            io.rData.valid := true.B
            rDataReg := reFillBuffer(bankOffset)
            rValidReg := true.B
          }
        }

        when(io.axi.r.bits.last) {
          // update the lru way register on the last cycle of refill
          // this will best reflect the LRU state
          lruWayReg := Mux(isSetNotFull, emptyPtr, lruLine)
          state := sWriteBack
        }
      }
    }
    is(sWriteBack) {
      // write the tag to the corresponding position
      tagWire := tagReg
      for (i <- 0 until wayAmount) {
        tagWe(i.U) := (i.U === lruWayReg)
      }
      // write the data to the corresbonding bank
      we(lruWayReg) := Seq.fill(bankAmount)(true.B)
      // update the valid to be true, whether it is before
      valid(lruWayReg)(indexReg) := true.B
      // update LRU to point at the refilled line
      updateLRU(lruWayReg)
      // preserve across the boundary in the state change
      checkRefillBufferHit()
      checkDCacheHit()

      state := sIdle
    }
  }
  // which index I'm visiting
  indexWire := Mux(state === sWriteBack, indexReg, index)

  val dataBanks = for {
    i <- 0 until wayAmount
    j <- 0 until bankAmount
  } yield {
    // word aligned banks
    val bank = Module(new SinglePortMaskBank(numberOfSet = setAmount, minWidth = 8, maskWidth = bankAmount, syncRead = true))
    // read and write share the address
    bank.io.addr := indexWire
    bank.io.we := we(i)(j)
    bank.io.writeData := reFillBuffer(j)
    bankData(i)(j) := bank.io.readData
  }
  val tagBanks = for (i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(setAmount, tagLen, syncRead = false))
    bank.io.we := tagWe(i)
    bank.io.addr := indexWire
    bank.io.inData := tagReg
    tagData(i) := bank.io.outData
  }

  //-----------------------------------------------------------------------------
  //------------------optional io for performance metrics--------------------------------------
  //-----------------------------------------------------------------------------
  if (performanceMonitorEnable) {
    // performance counter to count how many misses and how many hits are there
    val missCycleCounter = RegInit(0.U(64.W))
    val hitCycleCounter = RegInit(0.U(64.W))
    val idleCycleCounter = RegInit(0.U(64.W))
    when(io.rData.valid) {
      hitCycleCounter := hitCycleCounter + 1.U
    }.elsewhen(io.rData.enable && !io.rData.valid) {
        missCycleCounter := missCycleCounter + 1.U
      }
      .otherwise {
        idleCycleCounter := idleCycleCounter + 1.U
      }
    val performanceMonitorWire = Wire(new CachePerformanceMonitorIO)
    performanceMonitorWire.hitCycles := hitCycleCounter
    performanceMonitorWire.missCycles := missCycleCounter
    performanceMonitorWire.idleCycles := idleCycleCounter
    io.performanceMonitorIO.get := performanceMonitorWire
  }

  assert(io.axi.r.ready === true.B, "r ready signal should always be high")

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28, 0))
  }
}
