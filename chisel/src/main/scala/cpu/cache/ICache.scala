// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util._
import common.ValidBundle
import cpu.common.DefaultConfig._

/**
  * only for 2 way
  * |   tag     |   index     |   bankOffset      | 0.U(2.W)  | <Br/>
  * | `tagLen`  | `indexLen`  | `log2(bankAmount)`|     2     |
  */
//TODO: fix the "send the request next cycle" behaviour
//TODO: implement a pipeline
//TODO: way amount can't parameterize as of now
/**
  * generate icache with number of sets = depth, split amongst bank amount banks
  *
  * @param depth      how many sets there are in the i-cache
  * @param bankAmount how many cache banks there are for the cache lines
  */
class ICache(depth: Int = 128, bankAmount: Int = 16) extends Module {
  val wayAmount = 2 // 每组路数
  require(depth % 2 == 0, "depth of the cache must be a power of 2")
  val indexLen = log2Ceil(depth) // index宽度
  val bankSize = 32 / 8 // 每bank字节数
  val blockSize = bankAmount * bankSize // 每块字节数
  require(blockSize % 4 == 0, "the block size of the cache ( in number of bytes ) must be 4 aligned")
  val tagLen = 32 - indexLen - log2Ceil(blockSize) // tag宽度

  val io = IO(new Bundle {
    val busData = Input(new ValidBundle)

    val addr = Input(new ValidBundle)
    val inst = Output(UInt(dataLen.W))
    val hit = Output(Bool())
    val miss = Output(Bool())
  })
  //-------------------------------------------------------------------------------
  //--------------------set up the states and register of FSM----------------------
  val sIdle :: sMiss :: Nil = Enum(2)
  val lastState = RegInit(sIdle)
  val state = RegInit(sIdle)
  // cnt is a pseudo write mask, we should just use a real counter 0-7
  val cnt = RegInit(0.U(bankAmount.W))
  val miss = RegInit(false.B)

  //-------------------------------------------------------------------------------
  //-------------------- setup some values to use----------------------------------
  val tag = io.addr.bits(dataLen - 1, dataLen - tagLen)
  val index = io.addr.bits(dataLen - tagLen - 1, dataLen - tagLen - indexLen)
  val bankOffset = io.addr.bits(log2Ceil(blockSize) - 1, log2Ceil(bankSize))

  //-------------------------------------------------------------------------------
  //--------------------set up the memory banks and wire them to their states------
  // there should be no subword access here, so the whole line has only 1 valid
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
  //TODO: change the LRU to support more ways
  /** LRU(index) */
  val LRU = RegInit(VecInit(Seq.fill(depth)(0.U(1.W))))

  // setup variables that determines the current state
  val inAMiss = (state === sMiss)
  val lruLine = LRU(index)

  // check if there is a hit and the line that got the hit if there is a hit
  val tagMatch = Wire(Bool())
  tagMatch := false.B
  val hitWay = Wire(UInt(log2Ceil(wayAmount).W))
  hitWay := 0.U
  for (i <- 0 until wayAmount) {
    when(valid(i)(index) && tagData(i) === tag) {
      hitWay := i.U
      tagMatch := true.B
    }
  }

  //TODO: should I check for the LSB in the address here?
  val hit = state === sIdle && tagMatch || io.addr.bits(1, 0) =/= 0.U
  // here is what happens on a write during a miss
  // every cycle, 4 bytes of data will be transferred by AXI, exactly a bank

  lastState := state
  io.hit := hit

  // asserted until ar ready is asserted
  // if there is a miss, then don't change it; if miss is false and there is a miss, change it
  io.miss := miss

  io.inst := bankData(RegNext(hitWay))(RegNext(bankOffset))

  // the FSM transformation
  switch(state) {
    is(sIdle) {
      //TODO: make sure the fetch stage does not pass valid signal when the address is not aligned
      when(io.addr.valid) {
        miss := !tagMatch
        state := Mux(tagMatch, sIdle, sMiss)
        when(tagMatch) {
          // when there is a hit, update the LRU
          // if the way of the hit is the line lru
          // then update lru to point at the other line
          when(lruLine === hitWay) {
            LRU(index) := (~(hitWay.asBool)).asUInt()
          }
        }.otherwise {
          // during a miss, set the lowest bit of cnt to be true
          // this means we'll start writing from here
          // also, set io.miss to true. This will save a cycle
          io.miss := true.B
          cnt := 1.U
        }
      }
    }
    is(sMiss) {
      when(!cnt(bankAmount - 1) && io.busData.valid) {
        cnt := cnt << 1
        // on the first transfer of data, de-assert ar valid which
        // is io.miss here
        io.miss := false.B
      }.elsewhen(cnt(bankAmount - 1)) {
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
  when(inAMiss && io.busData.valid) {
    // write data into banks until all data has finished
    we(lruLine) := cnt.asBools()
    we((~(lruLine.asBool())).asUInt) := 0.U.asTypeOf(Vec(16, Bool()))
    // write the tag during the first cycle data returns from AXI
    when(cnt(0)) {
      tagWe(lruLine) := cnt(0).asBool()
      tagWe((~(lruLine.asBool())).asUInt) := false.B
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
    bank.io.inData := io.busData.bits
    bankData(i)(j) := bank.io.outData
  }
  val tagBanks = for (i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(depth, tagLen, syncRead = false))
    bank.io.we := tagWe(i)
    bank.io.addr := index
    bank.io.inData := tag
    tagData(i) := bank.io.outData
  }
}
