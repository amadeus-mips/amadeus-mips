// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util._
import common.AXIIO
import common.Constants._
import cpu.common.NiseSramReadIO
import cpu.common.DefaultConfig._
import cpu.performance.CachePerformanceMonitorIO

//TODO: will removing the fill buffer hurt performance?
//TODO: change into a read only interface
class ICacheAXIWrap(depth: Int = 128, bankAmount: Int = 16, performanceMonitorEnable: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val axi = AXIIO.master()
    val rInst = Flipped(new NiseSramReadIO)
    val performanceMonitorIO = if (performanceMonitorEnable) Some(new CachePerformanceMonitorIO) else None
  })

  //----------------------------------------------------------------
  //------------------set up the cache parameters-------------------
  //----------------------------------------------------------------
  val wayAmount = 2 // 每组路数
  val indexLen = log2Ceil(depth) // index宽度
  val bankSize = 32 / 8 // 每bank字节数
  val blockSize = bankAmount * bankSize // 每块字节数
  val tagLen = 32 - indexLen - log2Ceil(blockSize) // tag宽度

  //------------------------------------------------------------------------------------
  //------------------check if the generator parameters meeet requirements--------------
  //------------------------------------------------------------------------------------
  require(bankAmount <= 16 && bankAmount >= 1, s"bank amount is $bankAmount! Need between 1 and 16")
  require(depth % 2 == 0, "depth of the cache must be a power of 2")
  require(blockSize % 4 == 0, "the block size of the cache ( in number of bytes ) must be 4 aligned")

  //-------------------------------------------------------------------------------
  //--------------------set up the states and register of FSM----------------------
  //-------------------------------------------------------------------------------
  val sIdle :: sWait :: sFill :: Nil = Enum(3)
  val state = RegInit(sIdle)
  // cnt is a write mask to mark which to write to
  val cnt = RegInit(0.U(bankAmount.W))

  //-----------------------------------------------------------------------------
  //------------------assertions to check--------------------------------------
  //-----------------------------------------------------------------------------
  assert(!(io.rInst.valid && io.rInst.addr(1, 0).orR), "when address is not aligned, the valid signal must be false")

  //-------------------------------------------------------------------------------
  //-------------------- setup some constants to use----------------------------------
  //-------------------------------------------------------------------------------
  val addr = io.rInst.addr
  //TODO: check cached transaction
  val cachedTrans = true.B

  val tag = addr(dataLen - 1, dataLen - tagLen)
  val index = addr(dataLen - tagLen - 1, dataLen - tagLen - indexLen)
  val bankOffset = addr(log2Ceil(blockSize) - 1, log2Ceil(bankSize))

  //-------------------------------------------------------------------------------
  //--------------------set up the memory banks and wire them to their states------
  //-------------------------------------------------------------------------------
  /** valid(way)(index) */
  val valid = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(depth)(false.B)))))

  /** Write enable mask, we(way)(bank) */
  val we = Wire(Vec(wayAmount, Vec(bankAmount, Bool())))

  /** Tag write enable mask, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))

  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))

  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  // LRU points to the least recently used item in each set
  // we can do this because it is only 2 way set associative now
  /** LRU(index) */
  val LRU = RegInit(VecInit(Seq.fill(depth)(0.U(1.W))))

  //-----------------------------------------------------------------------------
  //------------------set up variables for this cycle----------------------------
  //-----------------------------------------------------------------------------

  // check what state we are in
  val isIdle = state === sIdle
  val isWait = state === sWait
  val isFill = state === sFill

  // check if there is a hit and the line that got the hit if there is a hit
  // despite its name, this indicates a true hit
  val isHit = WireDefault(false.B)

  // check which is the lru line
  val lruLine = LRU(index)

  // check if there is a hit and determine the way of the hit
  val hitWay = Wire(UInt(log2Ceil(wayAmount).W))
  hitWay := 0.U

  // check across all ways in the desired set
  for (i <- 0 until wayAmount) {
    when(valid(i)(index) && tagData(i) === tag) {
      hitWay := i.U
      isHit := true.B
    }
  }

  val instruction = Wire(UInt(dataLen.W))
  instruction := bankData(RegNext(hitWay))(RegNext(bankOffset))

  val axiARValid = WireDefault(isWait)
  //-----------------------------------------------------------------------------
  //------------------fsm transformation-----------------------------------------
  //-----------------------------------------------------------------------------
  // the FSM transformation
  //  switch(state) {
  //    is(sIdle) {
  //      when(io.rInst.enable) {
  //        miss := !isHit
  //        state := Mux(isHit, sIdle, sMiss)
  //        when(isHit) {
  //          // when there is a hit, update the LRU
  //          // if the way of the hit is the line lru
  //          // then update lru to point at the other line
  //          when(lruLine === hitWay) {
  //            LRU(index) := (~(hitWay.asBool)).asUInt()
  //          }
  //        }.otherwise {
  //          // during a miss, set the lowest bit of cnt to be true
  //          // this means we'll start writing from here
  //          // also, set io.miss to true. This will save a cycle
  //          missWire := true.B
  //          cnt := 1.U
  //        }
  //      }
  //    }
  //    is(sMiss) {
  //      //TODO: reformat this
  //      when(!cnt(bankAmount - 1) && io.axi.r.bits.id === INST_ID && io.axi.r.valid) {
  //        cnt := cnt << 1
  //        // on the first transfer of data, de-assert ar valid which
  //        // is io.miss here
  //        missWire := false.B
  //      }.elsewhen(cnt(bankAmount - 1)) {
  //        valid(lruLine)(index) := true.B
  //        cnt := 0.U
  //        state := sIdle
  //      }
  //    }
  //  }
  switch(state) {
    is(sIdle) {
      //TODO: a way to simplify nested when
      when(io.rInst.enable) {
        // enable already ensures that the address is aligned
        when(isHit) {
          LRU(index) := Mux(lruLine === hitWay, (~hitWay.asBool).asUInt(), lruLine)
        }.otherwise {
          cnt := 1.U
          state := sWait
          // override the ar valid signal, this will advance one cycle
          axiARValid := true.B
        }
      }
    }
    is(sWait) {
      when(io.axi.r.bits.id === INST_ID && io.axi.r.fire) {
        state := sFill
        cnt := cnt << 1
      }
    }
    is(sFill) {
      cnt := cnt << 1
      when(cnt(bankAmount - 1).asBool) {
        valid(lruLine)(index) := true.B
        cnt := 0.U
        state := sIdle
      }
    }
  }

  we := 0.U.asTypeOf(Vec(wayAmount, Vec(bankAmount, Bool())))
  tagWe := 0.U.asTypeOf(Vec(wayAmount, Bool()))

  /**
    * what happens during a miss
    * wait for AXI to return data
    * write the tag on the first cycle when data returns
    * write to each bank consequently
    */
  when(isWait && io.axi.r.bits.id === INST_ID && io.axi.r.fire) {
    // write data into banks until all data has finished
    we(lruLine) := cnt.asBools()
    we((~lruLine.asBool()).asUInt) := 0.U.asTypeOf(Vec(16, Bool()))
    // write the tag during the first cycle data returns from AXI
    when(cnt(0)) {
      tagWe(lruLine) := cnt(0).asBool()
      tagWe((~lruLine.asBool()).asUInt) := false.B
    }
  }

  val instBanks = for {
    i <- 0 until wayAmount
    j <- 0 until bankAmount
  } yield {
    val bank = Module(new SinglePortBank(depth, dataLen, syncRead = true))
    bank.io.we := we(i)(j)
    bank.io.en.get := true.B
    bank.io.addr := index
    bank.io.inData := io.axi.r.bits.data
    bankData(i)(j) := bank.io.outData
  }
  val tagBanks = for (i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(depth, tagLen, syncRead = false))
    bank.io.we := tagWe(i)
    bank.io.addr := index
    bank.io.inData := tag
    tagData(i) := bank.io.outData
  }

  //-----------------------------------------------------------------------------
  //------------------optional io for performance metrics--------------------------------------
  //-----------------------------------------------------------------------------
  if (performanceMonitorEnable) {
    // performance counter to count how many misses and how many hits are there
    val missCycleCounter = RegInit(0.U(32.W))
    val hitCycleCounter = RegInit(0.U(32.W))
    when(state === sIdle) {
      hitCycleCounter := hitCycleCounter + 1.U
    }.otherwise {
      missCycleCounter := missCycleCounter + 1.U
    }
    val performanceMonitorWire = Wire(new CachePerformanceMonitorIO)
    performanceMonitorWire.hitCycles := hitCycleCounter
    performanceMonitorWire.missCycles := missCycleCounter
    io.performanceMonitorIO.get := performanceMonitorWire
  }

  //-----------------------------------------------------------------------------
  //------------------set up io between cpu and axi------------------------------
  //-----------------------------------------------------------------------------
  io.axi := DontCare

  io.axi.ar.bits.id := INST_ID
  io.axi.ar.bits.addr := Mux(
    cachedTrans,
    Cat(0.U(3.W), addr(28, 2 + log2Ceil(bankAmount)), 0.U((2 + log2Ceil(bankAmount)).W)),
    virToPhy(addr)
  )
  io.axi.ar.bits.len := Mux(cachedTrans, (bankAmount - 1).U(4.W), 0.U(4.W)) // 16 or 1
  io.axi.ar.bits.size := "b010".U(3.W) // 4 Bytes
  io.axi.ar.bits.burst := "b01".U(2.W) // Incrementing-address burst

  /**
    * axi specification requires that ar valid is asserted until
    * the rising clock edge after the slave asserts the ARREADY signal.
    */
  io.axi.ar.valid := axiARValid

  // these are hard wired as required by Loongson
  io.axi.ar.bits.lock := 0.U
  //TODO: can we utilize this?
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot := 0.U
  // hardcode r to always be ready
  //TODO: check in case of problem
  io.axi.r.ready := true.B

  io.rInst.valid := cachedTrans && isIdle && isHit && io.rInst.enable
  io.rInst.data := instruction

  //  iCache.io.busData.bits := io.axi.r.bits.data
  //  iCache.io.busData.valid := io.axi.r.bits.id === INST_ID && io.axi.r.valid
  //  iCache.io.addr.bits := io.rInst.addr
  //  iCache.io.addr.valid := io.rInst.enable

  /** just erase high 3 bits */
  def virToPhy(addr: UInt): UInt = {
    require(addr.getWidth == addrLen)
    Cat(0.U(3.W), addr(28, 0))
  }
}
