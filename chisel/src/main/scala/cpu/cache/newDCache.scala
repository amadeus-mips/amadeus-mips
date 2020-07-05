// See README.md for license details.

package cpu.cache

import chisel3._
import cpu.common.NiseSramWriteIO
import shared.CircularShifterInt
import shared.LRU.PLRUMRUNM
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
//TODO: Don't make untranslated/uncached go through d-cache
//TODO: Don't get in the way of the critical path
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
    val rChannel = Flipped(new NiseSramReadIO)
    val wChannel = Flipped(new NiseSramWriteIO)
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
  val sIdle :: sTransfer :: sWriteFinish :: sWaitForAR :: sReFill :: sWriteBack :: Nil = Enum(6)
  val state = RegInit(sIdle)

  // this is really just an int, tracking which index I'm writing to in
  // the refill buffer, i.e. next target
  val refillWriteMask = Module(new CircularShifterInt(bankAmount))

  // write vec marks which words in the refill buffer has been written to i.e. per word valid
  val refillWriteVec = RegInit(VecInit(Seq.fill(bankAmount)(false.B)))

  // whether there is a write to the write vec
  val refillWriteVecDirty = RegInit(false.B)

  // refill buffer is a buffer for holding values from axi r channel
  // at the end of the refill states this buffer will be re-written to
  // the d-cache line
  val reFillBuffer = RegInit(VecInit(Seq.fill(bankAmount)(VecInit(Seq.fill(4)(0.U(8.W))))))
//  RegInit(VecInit(Seq.fill(bankAmount)(0.U(32.W))))

  // record the memory address info on a read miss
  // keep track of which way to write to
  val lruWayReg = Reg(UInt(log2Ceil(wayAmount).W))

  // record which way is LRU on a write miss
  // this requires the LRU line is dirty
  val writeMissWayReg = Reg(UInt(log2Ceil(wayAmount).W))

  // keep track of the index
  val indexReg = Reg(UInt(indexLen.W))

  // keep track of the tag
  val tagReg = Reg(UInt(tagLen.W))

  // keep track of the specific index of the bank
  val bankOffsetReg = Reg(UInt(log2Ceil(bankAmount).W))

  // Notice: this won't be true across rWriteBack to rIdle, as there is
  // no lookup during rWriteBack
  // hit in the reFill buffer during refill stage
  val rChannelReg = RegNext(io.axi.r.bits.data)
  val rValidReg = RegInit(false.B)

  // hit under miss in d-cache, set this register
  // hit in the D-cache during reFill stage
  val rDCacheHitReg = RegInit(false.B)

  // records if the current set is full
  // this is to make sure empty lines are filled ahead of LRU
  val isSetNotFull = WireDefault(false.B)
  val emptyPtr = WireDefault(0.U(log2Ceil(wayAmount).W))

  // keep track of whether there has been an aw handshake yet
  // true means still waiting, false means that the handshake has finished
  // only assert before waiting for handshake
  val waitForAWHandshake = RegInit(false.B)

  // the buffer that holds the data to be dispatched to axi write
  val writeDataBuffer = Reg(Vec(bankAmount, UInt(32.W)))

  // the counter that records which word to dispatch to axi from write data buffer
  val writeBufferCounter = RegInit(0.U(log2Ceil(bankAmount).W))

  // hold the dispatch to write address
  val writeAddrReg = RegInit(0.U(32.W))
  //-----------------------------------------------------------------------------
  //------------------assertions to check--------------------------------------
  //-----------------------------------------------------------------------------
//  assert(!(io.rChannel.valid && io.rChannel.addr(1, 0).orR), "when address is not aligned, the valid signal must be false")
  assert(
    !(io.rChannel.valid && !io.rChannel.enable),
    "the returned data should not be valid when the address is not enabled"
  )
  //-------------------------------------------------------------------------------
  //-------------------- setup some constants to use----------------------------------
  //-------------------------------------------------------------------------------
  val addr = io.rChannel.addr

  val tag = addr(dataLen - 1, dataLen - tagLen)
  val index = addr(dataLen - tagLen - 1, dataLen - tagLen - indexLen)
  val bankOffset = addr(log2Ceil(blockSize) - 1, log2Ceil(bankSize))

  //-------------------------------------------------------------------------------
  //--------------------set up the memory banks and wire them to their states------
  //-------------------------------------------------------------------------------
  /** valid(index)(way) */
  val valid = RegInit(VecInit(Seq.fill(setAmount)(VecInit(Seq.fill(wayAmount)(false.B)))))

  /** dirty(index)(way) */
  val dirty = RegInit(VecInit(Seq.fill(setAmount)(VecInit(Seq.fill(wayAmount)(false.B)))))

  /** Write enable mask, we(way)(bank) */
  val we = Wire(Vec(wayAmount, Vec(bankAmount, Bool())))

  /** Tag write enable mask, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))

  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))

  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  // there are several backends for LRU, mru performs better than tree
  val LRU = PLRUMRUNM(numOfSets = setAmount, numOfWay = wayAmount)

  val tagWire = Wire(UInt(tagLen.W))

  val indexWire = Wire(UInt(indexLen.W))
  //-----------------------------------------------------------------------------
  //------------------set up variables for this cycle----------------------------
  //-----------------------------------------------------------------------------
  tagWire := tag
  // check what state we are in
  //TODO: notice this does not precisely imply the state, as it state update happens
  // within the cycle
  val isIdle = state === sIdle
  val isWaitForAR = state === sWaitForAR
  val isReFill = state === sReFill
  val isWriteBack = state === sWriteBack

  // check if there is a hit and the line that got the hit if there is a hit
  // despite its name, this indicates a true hit
  // this hit works both for read hit and write hit
  val isHit = WireDefault(false.B)

  // check during reFill whether tag and index are the same as old
  val isTagSameAsOld = tag === tagReg
  val isIndexSameAsOld = index === indexReg
  val isBankOffsetSameAsOld = bankOffset === bankOffsetReg

  // check which is the lru line
  val lruLine = LRU.getLRU(index)

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
  refillWriteMask.io.initPosition.bits  := 0.U
  refillWriteMask.io.initPosition.valid := false.B
  refillWriteMask.io.shiftEnable        := false.B

  io.axi := DontCare

  io.axi.ar.bits.id := DATA_ID
  io.axi.ar.bits.addr :=
    Mux(state === sIdle, addr, Cat(tagReg, indexReg, bankOffsetReg, 0.U(2.W)))

  io.axi.ar.bits.len   := (bankAmount - 1).U(4.W)
  io.axi.ar.bits.size  := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b10".U(2.W) // wrap burst

  /**
    * there was a design where if there is a miss, assert valid immediately
    * maybe it is not a good idea, need to know how this translate to verilog
    */
  io.axi.ar.valid := isWaitForAR

  //TODO: change the R-ready signal
  //  io.axi.r.ready := state === sReFill
  io.axi.r.ready := true.B

  // these are hard wired as required by Loongson
  io.axi.ar.bits.lock := 0.U
  //TODO: can we utilize this?
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot  := 0.U

  /**
    * default IO for aw
    */
  io.axi.aw.bits.id    := DATA_ID
  io.axi.aw.bits.len   := (bankAmount - 1).U(4.W)
  io.axi.aw.bits.size  := "b010".U(3.W)
  io.axi.aw.bits.burst := "b10".U(2.W)
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.bits.lock  := 0.U

  io.axi.aw.bits.addr := writeAddrReg

  // aw handshake has not taken place, and is in the transfer state
  //TODO: this is kind of out of sync with the states
  io.axi.aw.valid := waitForAWHandshake && state === sTransfer
//  assert(
//    state =/= sTransfer && waitForAWHandshake,
//    "when state is not in transfer, wait for aw handshake signal should be low"
//  )

  io.axi.w.bits.id   := DATA_ID
  io.axi.w.bits.strb := "b1111".U(4.W)
  io.axi.w.bits.last := false.B
  io.axi.w.valid     := state === sTransfer
  io.axi.w.bits.data := DontCare // handled later

  io.axi.b.ready := state === sWriteFinish

  // the contents for a read
  val cacheContents = Wire(UInt(dataLen.W))
  val hitWayReg = RegNext(hitWay)
  val bankOffSetNextReg = RegNext(bankOffset)
  cacheContents := bankData(hitWayReg)(bankOffSetNextReg)
  // read data is only valid when read enable is asserted
  io.rChannel.valid := isIdle && isHit && io.rChannel.enable
  io.rChannel.data := cacheContents

  // a write is successful the state is idle and write enable is asserted
  io.wChannel.valid := isIdle && isHit && io.wChannel.enable

  val isSetNotFullReg = RegInit(false.B)
  val emptyPtrReg = RegInit(0.U(log2Ceil(wayAmount).W))
  val lruLineReg = RegInit(0.U(log2Ceil(wayAmount).W))
  val lruSelWay = Mux(isSetNotFullReg, emptyPtrReg, lruLineReg)

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
    reFillBuffer   := 0.U.asTypeOf(reFillBuffer)
    refillWriteVecDirty := false.B
    // don't invalidate this line
  }

  def recordMemAddress(): Unit = {
    indexReg      := index
    tagReg        := tag
    bankOffsetReg := bankOffset
  }

  /**
    * this is to check whether there is a hit in the
    * d-cache when the d-cache is under a miss
    * if there is a hit, send the cacheContents ( this cycle )
    * to the rChannel, valid has been asserted last cycle
    */
  def checkPreviousDCacheHit(): Unit = {
    when(rDCacheHitReg) {
      rDCacheHitReg    := false.B
      io.rChannel.data := cacheContents
    }
  }

  /**
    * check if there is a hit in the refill buffer
    * this is to preserve the result across the fsm
    * state boundary
    */
  def checkPreviousRefillBufferHit(): Unit = {
    when(rValidReg) {
      rValidReg        := false.B
      io.rChannel.data := rChannelReg
    }
  }

  /**
    * invalidate the cache line
    * @param evictWay from which way to evict
    * @param setIndex which is the index to evict
    */
  def invalidateLine(evictWay: UInt, setIndex: UInt): Unit = {
    // make the set not full
    // invalidate now and dispatch to buffer
    valid(setIndex)(evictWay) := false.B
    dirty(setIndex)(evictWay) := false.B
  }

  def dispatchToWrite(waySelect: UInt, indexSelect: UInt): Unit = {
    writeMissWayReg    := waySelect
    waitForAWHandshake := true.B
    writeDataBuffer    := bankData(waySelect)
    writeBufferCounter := 0.U
    writeAddrReg := Cat(tagData(waySelect), indexReg, 0.U((log2Ceil(bankAmount) + 2).W))
    assert(Cat(tagData(waySelect), indexReg, 0.U((log2Ceil(bankAmount) + 2).W)).getWidth == 32)
  }

  /**
    * handle a read miss
    * if the line is dirty and set is full, invalidate the line and dispatch to write
    * if the line is not dirty or set is not full, enter read miss
    */
  def handleReadMiss(): Unit = {
    // now we are on a write miss
    // check if LRU is dirty
    recordMemAddress()
    // when LRU line is dirty and set is not full
    // enter read miss
    state := sWaitForAR
  }

  val tagBanks = for (i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(setAmount, tagLen, syncRead = false))
    bank.suggestName(s"tagBank_way_${i}")
    bank.io.we := tagWe(i)
    bank.io.addr := Mux(isReFill && io.axi.r.bits.last, indexReg, indexWire)
    bank.io.inData := tagReg
    tagData(i) := bank.io.outData
  }

  /**
    * check if there is a hit in the refill buffer
    */
  def checkRefillBufferHit(): Unit = {
    /** if the reFill buffer could have a hit */
    when(refillWriteVec(bankOffset) && isTagSameAsOld && isIndexSameAsOld) {
      // if it is a read hit, buffer the result, assert read valid
      when(io.rChannel.enable) {
        // if the hit occurs in the refill buffer
        io.rChannel.valid := true.B
        rChannelReg := reFillBuffer(bankOffset).asUInt
        rValidReg := true.B
      }.elsewhen(io.wChannel.enable) {
        // if it is a write hit, write into reFill buffer
        for ( i <- 0 until 4 ) {
          when(io.wChannel.sel(i)) {
            reFillBuffer(bankOffset)(i) := io.wChannel.data(i * 8 + 7, i * 8)
          }
        }
        io.wChannel.valid := true.B
        refillWriteVecDirty := true.B
      }
    }
  }

  def handleWriteHit(): Unit = {
    // flag wChannel valid to show that this operation has completed
    io.wChannel.valid := true.B

    // write to the way that is hit
    we(hitWay)(bankOffset) := true.B
    // by default, the write mask is connected
    // make the line dirty
    dirty(index)(hitWay) := true.B
    // update the LRU
    LRU.update(index, hitWay)
  }

  //-----------------------------------------------------------------------------
  //------------------fsm transformation-----------------------------------------
  //-----------------------------------------------------------------------------
  switch(state) {
    is(sIdle) {
      assert(
        !(io.rChannel.enable && io.wChannel.enable),
        "read channel and write channel should not be accessed at the same time"
      )
      when(io.rChannel.enable) {
        // enable already ensures that the address is aligned
        when(isHit) {
          LRU.update(index, hitWay)
        }.otherwise {
          handleReadMiss()
        }
      }.elsewhen(io.wChannel.enable) {
        when(isHit) {
          handleWriteHit()
        }.otherwise {
          handleReadMiss()
        }
      }
    }
    is(sWaitForAR) {
      when(io.axi.ar.fire) {
        beginRTransaction()
      }
    }
    is(sReFill) {
//      assert(io.axi.r.bits.id === DATA_ID, "r id is not supposed to be different from d-cache id")
      assert(!io.axi.r.valid || (io.axi.r.bits.resp === 0.U), "the response should always be okay")

      io.rChannel.valid := false.B

      // this defaults to false, as this value is only used in the write back state
      rValidReg := false.B

      checkPreviousDCacheHit()
      checkPreviousRefillBufferHit()

      checkRefillBufferHit()


      // with every successful transaction, increment the bank offset register to reflect the new value
      when(io.axi.r.fire) {
        // update the write mask
        refillWriteMask.io.shiftEnable := true.B

        // write to the refill buffer instead of the data bank
        reFillBuffer(refillWriteMask.io.vector) := io.axi.r.bits.data.asTypeOf(Vec(4, UInt(8.W)))
        // update the vec to record which positions has been written to
        refillWriteVec(refillWriteMask.io.vector) := true.B

        // write to each bank consequently
        bankOffsetReg := bankOffsetReg + 1.U

        isSetNotFullReg := isSetNotFull
        emptyPtrReg := emptyPtr
        lruLineReg := lruLine

        //TODO: performance counter: how many writes will be issued?
        when(io.axi.r.bits.last) {
          // update the lru way register on the last cycle of refill
          // this will best reflect the LRU state
          lruWayReg := lruSelWay
          when(dirty(indexReg)(lruSelWay)) {
            invalidateLine(lruSelWay, indexReg)
            dispatchToWrite(lruSelWay, indexReg)
          }
          state := sWriteBack
        }.otherwise {
          // in the rlast cycle tag will be accessed to evict the dirty line
          checkDCacheHit()
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
      dirty(indexReg)(lruWayReg) := refillWriteVecDirty
      // update LRU to point at the refilled line
      LRU.update(indexReg, lruWayReg)
      // preserve across the boundary in the state change
      checkPreviousRefillBufferHit()
      checkPreviousDCacheHit()

      state := Mux(waitForAWHandshake, sTransfer, sIdle)
    }
    is(sTransfer) {
      checkPreviousDCacheHit()
      checkDCacheHit()
      // after an aw handshake, waitfor signal is de asserted
      // aw valid is de asserted
      when(io.axi.aw.fire) {
        waitForAWHandshake := false.B
      }

      when(io.axi.w.fire()) {
        // increment the write counter
        writeBufferCounter := writeBufferCounter + 1.U
        io.axi.w.bits.data := writeDataBuffer(writeBufferCounter)
        // when all line has been evicted
        when(writeBufferCounter === (bankAmount - 1).U) {
          io.axi.w.bits.last := true.B
          state := sWriteFinish
          // let's handle the b channel there
        }
      }


    }
    is(sWriteFinish) {
      // when b successfully handshakes
      when(io.axi.b.fire) {
        assert(io.axi.b.bits.id === DATA_ID)
        assert(io.axi.b.bits.resp === 0.U)
        state := sIdle
      }
    }
  }
  // which index I'm visiting
  indexWire := Mux(state === sWriteBack, indexReg, index)

  val dataBanks = for {
    i <- 0 until wayAmount
    j <- 0 until bankAmount
  } yield {
    // word aligned banks
    val bank = Module(
      new SinglePortMaskBank(numberOfSet = setAmount, minWidth = 8, maskWidth = 4, syncRead = true)
    )
    bank.suggestName(s"dbank_way${i}_bankOffset${j}")
    // read and write share the address
    bank.io.addr := indexWire
    bank.io.we := we(i)(j)
    // if during the write back stage, then only write the refill buffer back.
    // otherwise (during idle stage) write the data from wChannel
    bank.io.writeData := Mux(isWriteBack, reFillBuffer(j).asUInt, io.wChannel.data)
    bank.io.writeMask := Mux(isWriteBack, 15.U(4.W), io.wChannel.sel)
    bankData(i)(j) := bank.io.readData
    bank.desiredName
  }

  def checkDCacheHit(): Unit = {
    when(isHit) {
      when(io.rChannel.enable) {
        io.rChannel.valid := true.B
        rDCacheHitReg := true.B
        LRU.update(index, hitWay)
      }.elsewhen(io.wChannel.enable) {
        //TODO: check this and get it out of the critical path
        // Don't write to underlying banks when r last, dirty will be discarded if
        // in the same index, same way
        handleWriteHit()
      }
    }
  }

  //-----------------------------------------------------------------------------
  //------------------optional io for performance metrics--------------------------------------
  //-----------------------------------------------------------------------------
  if (performanceMonitorEnable) {
    // performance counter to count how many misses and how many hits are there
    val missCycleCounter = RegInit(0.U(64.W))
    val hitCycleCounter = RegInit(0.U(64.W))
    val idleCycleCounter = RegInit(0.U(64.W))
    when(io.rChannel.valid) {
      hitCycleCounter := hitCycleCounter + 1.U
    }.elsewhen(io.rChannel.enable && !io.rChannel.valid) {
        missCycleCounter := missCycleCounter + 1.U
      }
      .otherwise {
        idleCycleCounter := idleCycleCounter + 1.U
      }
    val performanceMonitorWire = Wire(new CachePerformanceMonitorIO)
    performanceMonitorWire.hitCycles  := hitCycleCounter
    performanceMonitorWire.missCycles := missCycleCounter
    performanceMonitorWire.idleCycles := idleCycleCounter
    io.performanceMonitorIO.get       := performanceMonitorWire
  }

  assert(io.axi.r.ready === true.B, "r ready signal should always be high")

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28, 0))
  }
}
