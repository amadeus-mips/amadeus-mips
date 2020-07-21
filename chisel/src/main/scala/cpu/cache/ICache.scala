// See README.md for license details.

package cpu.cache

import chisel3._
import shared.CircularShifterInt
import shared.LRU.PseudoLRUMRU
//import chisel3.util.{log2Ceil, Cat}
import axi.AXIIO
import chisel3.util._
import cpu.common.DefaultConfig._
import cpu.common.NiseSramReadIO
import cpu.performance.CachePerformanceMonitorIO
import shared.Constants._

//TODO: discuss propagating the signal
//TODO: change into a read only interface
//TODO: be able to invalidate the refill buffer
//TODO: optimize axi port
/**
  * icache with an AXI interface
  * @param setAmount how many sets there are in the i-cache
  * @param wayAmount how many ways there are in each set of i-cache
  * @param bankAmount how many banks there are in the i-cache
  * @param performanceMonitorEnable whether to enable the performance metrics
  */
class ICache(
  setAmount:                Int = 64,
  wayAmount:                Int = 4,
  bankAmount:               Int = 16,
  performanceMonitorEnable: Boolean = false
) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val rInst = Flipped(new NiseSramReadIO)
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
  val sIdle :: sWaitForAR :: sReFill :: sWriteBack :: Nil = Enum(4)
  val state = RegInit(sIdle)

  // this is really just an int, tracking which index I'm writing to in
  // the refill buffer
  val writeMask = Module(new CircularShifterInt(bankAmount))

  // write vec marks which words in the refill buffer has been written to
  val writeVec = RegInit(VecInit(Seq.fill(bankAmount)(false.B)))
  // refill buffer is a buffer for holding values from axi r channel
  // at the end of the refill states this buffer will be re-written to
  // the i-cache line
  val reFillBuffer = RegInit(VecInit(Seq.fill(bankAmount)(0.U(32.W))))

  // keep track of which way to write to
  //TODO: which cycle should this be determined
  val lruWayReg = Reg(UInt(log2Ceil(wayAmount).W))
  // keep track of the index
  val indexReg = Reg(UInt(indexLen.W))
  // keep track of the tag
  val tagReg = Reg(UInt(tagLen.W))
  // keep track of the specific index of the bank
  val bankOffsetReg = Reg(UInt(log2Ceil(bankAmount).W))

  // register for preserving the r data across rlast
  val rDataReg = RegNext(io.axi.r.bits.data)
  val rValidReg = RegInit(false.B)

  // hit under miss in i-cache, set this register
  val rICacheHitReg = RegInit(false.B)

  // records if the current set is full
  // this is to make sure empty lines are filled ahead of LRU
  val isSetNotFull = WireDefault(false.B)
  val emptyPtr = WireDefault(0.U(log2Ceil(wayAmount).W))
  //-----------------------------------------------------------------------------
  //------------------assertions to check--------------------------------------
  //-----------------------------------------------------------------------------
  assert(!(io.rInst.valid && io.rInst.addr(1, 0).orR), "when request is not aligned, the valid signal must be false")
  assert(!(io.rInst.valid && !io.rInst.enable), "the returned data should not be valid when the request is not enabled")
  //-------------------------------------------------------------------------------
  //-------------------- setup some constants to use----------------------------------
  //-------------------------------------------------------------------------------
  val addr = io.rInst.addr

  val tag = addr(dataLen - 1, dataLen - tagLen)
  val index = addr(dataLen - tagLen - 1, dataLen - tagLen - indexLen)
  val bankOffset = addr(log2Ceil(blockSize) - 1, log2Ceil(bankSize))

  //-------------------------------------------------------------------------------
  //--------------------set up the memory banks and wire them to their states------
  //-------------------------------------------------------------------------------
  /** valid(index)(way) */
  //TODO: change the order if it is not banked
  val valid = RegInit(VecInit(Seq.fill(setAmount)(VecInit(Seq.fill(wayAmount)(false.B)))))

  /** Write enable mask, we(way)(bank) */
  val we = Wire(Vec(wayAmount, Vec(bankAmount, Bool())))

  /** Tag write enable mask, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))

  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))

  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  // there are several backends for LRU, mru performs better than tree
  val LRU = Module(new PseudoLRUMRU( numOfSets = setAmount, numOfWay = wayAmount))

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

  // check during reFill whether tag and index are the same as old
  val isTagSameAsOld = tag === tagReg
  val isIndexSameAsOld = index === indexReg
  val isBankOffsetSameAsOld = bankOffset === bankOffsetReg

  // check if there is a hit and the line that got the hit if there is a hit
  // despite its name, this indicates a true hit
  val isHit = WireDefault(false.B)

  // check which is the lru line
  val lruLine = LRU.io.lruLine

  // check if there is a hit and determine the way of the hit
  val hitWay = Wire(UInt(log2Ceil(wayAmount).W))
  hitWay := 0.U

  // check across all ways in the desired set
  isSetNotFull := valid(indexReg).contains(false.B)
  emptyPtr := valid(indexReg).indexWhere(isWayValid => !isWayValid)

  val tagCheckVec = Wire(Vec(wayAmount, Bool()))
  tagCheckVec := (tagData.map( _ === tag) zip valid(index)).map{case (tagMatch, isValid) => tagMatch && isValid}
  hitWay := tagCheckVec.indexWhere(tagMatchAndValid => tagMatchAndValid  === true.B)
  isHit := tagCheckVec.contains(true.B)


  //-----------------------------------------------------------------------------
  //------------------initialize default IO--------------------------------
  //-----------------------------------------------------------------------------

  // by default, don't write back to bank
  we := 0.U.asTypeOf(Vec(wayAmount, Vec(bankAmount, Bool())))
  // by default, don't change the tag
  tagWe := 0.U.asTypeOf(Vec(wayAmount, Bool()))

  // default io for write mask
  writeMask.io.initPosition.bits := 0.U
  writeMask.io.initPosition.valid := false.B
  writeMask.io.shiftEnable := false.B

  io.axi := DontCare

  io.axi.ar.bits.id := INST_ID
  io.axi.ar.bits.addr :=
    Mux(state === sIdle, addr, Cat(tagReg, indexReg, bankOffsetReg, 0.U(2.W)))


  io.axi.ar.bits.len := (bankAmount - 1).U(4.W)
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b10".U(2.W) // wrap burst

  /**
    * there was a design where if there is a miss, assert valid immediately
    * maybe it is not a good idea, need to know how this translate to verilog
    */
  io.axi.ar.valid := isWaitForAR


  // these are hard wired as required by Loongson
  io.axi.ar.bits.lock := 0.U
  //TODO: can we utilize this?
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U

  io.axi.r.ready := state === sReFill

  val instruction = Wire(UInt(dataLen.W))
  val hitWayReg = RegNext(hitWay)
  val bankOffSetNextReg = RegNext(bankOffset)
  instruction := bankData(hitWayReg)(bankOffSetNextReg)
  io.rInst.valid := isIdle && isHit && io.rInst.enable
  io.rInst.data := instruction

  LRU.io.accessEnable := false.B
  LRU.io.accessWay := DontCare
  LRU.io.accessSet := index

  //-----------------------------------------------------------------------------
  //------------------transaction as functions-----------------------------------
  //-----------------------------------------------------------------------------
  def beginRTransaction(): Unit = {
    state := sReFill
    writeMask.io.initPosition.valid := true.B
    writeMask.io.initPosition.bits := bankOffsetReg
    // the precise timing of this should happen at the first cycle of the r transaction
    // however, as it is put into the refill state ( because if you wait for R, then
    // you can't handle the first receive unless you put it into a reg, but that's kind
    // of ugly
    writeVec := 0.U.asTypeOf(writeVec)
    reFillBuffer := 0.U.asTypeOf(reFillBuffer)
    // don't invalidate this line
  }

  def beginARTransaction(): Unit = {
    indexReg := index
    tagReg := tag
    bankOffsetReg := bankOffset
  }

  /**
    * this is to check whether there is a hit in the
    * I-cache when the i-cache is under a miss
    * if there is a hit, send the instruction ( this cycle )
    * to the rInst, valid has been asserted last cycle
    */
  def checkICacheHit(): Unit = {
    when(rICacheHitReg) {
      rICacheHitReg := false.B
      io.rInst.data := instruction
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
      io.rInst.data := rDataReg
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
      // TODO: check last boundary crossing

      when(io.rInst.enable) {
        // enable already ensures that the request is aligned
        when(isHit) {
          updateLRU(hitWay)
        }.otherwise {
          state := sWaitForAR
          beginARTransaction()
        }
      }
    }
    is(sWaitForAR) {
      when(io.axi.ar.fire) {
        beginRTransaction()
      }
    }
    is(sReFill) {
      assert(io.axi.r.bits.id === INST_ID, "r id is not supposed to be different from i-cache id")
      assert(io.axi.r.bits.resp === 0.U, "the response should always be okay")

      io.rInst.valid := false.B
      // by default, this is connected to the rData register that preserves the hit
      // in refill buffer or axi from the previous cycle, this could also be connected
      // to i-cache ( instruction variable )
      io.rInst.data := rDataReg
      // this defaults to false, as this value is only used in the write back state
      rValidReg := false.B
      checkICacheHit()

      /**
        * check if there is a hit in the I-cache
        */
      when(isHit && io.rInst.enable) {
        io.rInst.valid := true.B
        rICacheHitReg := true.B
        updateLRU(hitWay)
      }

      // check if there is a hit in the write vec
      when(writeVec(bankOffset) && io.rInst.enable && isTagSameAsOld && isIndexSameAsOld) {
        // if the hit occurs in the refill buffer
        io.rInst.valid := true.B
        rDataReg := reFillBuffer(bankOffset)
        rValidReg := true.B
      }

      // with every successful transaction, increment the bank offset register to reflect the new value
      when(io.axi.r.fire) {
        // update the write mask
        writeMask.io.shiftEnable := true.B

        // write to the refill buffer instead of the data bank
        reFillBuffer(writeMask.io.vector) := io.axi.r.bits.data
        // update the vec to record which positions has been written to
        writeVec(writeMask.io.vector) := true.B

        // write to each bank consequently
        bankOffsetReg := bankOffsetReg + 1.U

        // refill is hit when tag is a hit, and index is a hit, and the data from axi is ready
        // when index and tag are hit
        when(isTagSameAsOld && isIndexSameAsOld && io.rInst.enable && isBankOffsetSameAsOld) {
          // when there is a direct hit from the bank
            io.rInst.valid := true.B
            rValidReg := true.B
            // r data reg = axi r bits by default
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
      valid(indexReg)(lruWayReg) := true.B
      // update LRU to point at the refilled line
      updateLRU(lruWayReg)
      // preserve across the boundary in the state change
      checkRefillBufferHit()
      checkICacheHit()

      state := sIdle
    }
  }
  // which index I'm visiting
  indexWire := Mux(state === sWriteBack, indexReg, index)

  val instBanks = for {
    i <- 0 until wayAmount
    j <- 0 until bankAmount
  } yield {
    val bank = Module(new SinglePortBank(setAmount, dataLen, syncRead = true))
    bank.suggestName(s"dbank_way${i}_bankOffset${j}")
    bank.io.we := we(i)(j)
    bank.io.en.get := true.B
    bank.io.addr := indexWire
    bank.io.inData := reFillBuffer(j)
    bankData(i)(j) := bank.io.outData
  }
  val tagBanks = for (i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(setAmount, tagLen, syncRead = false))
    bank.suggestName(s"tagBank_bankOffset${i}")
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
    when(io.rInst.valid) {
      hitCycleCounter := hitCycleCounter + 1.U
    }.elsewhen(io.rInst.enable && !io.rInst.valid) {
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

}
