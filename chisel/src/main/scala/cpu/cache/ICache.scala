// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util._
import common.ValidBundle
import cpu.common.DefaultConfig._

/**
 *  only for 2 way
 *  |   tag     |   index     |   bankOffset      | 0.U(2.W)  | <Br/>
 *  | `tagLen`  | `indexLen`  | `log2(bankAmount)`|     2     |
 */
class ICache(depth: Int = 128, bankAmount: Int = 16) extends Module {
  val wayAmount = 2   // 每组路数
//  val depth = 128     // 组数
  val indexLen = log2Ceil(depth)  // index宽度
  val bankSize = 32/8       // 每bank字节数
  val blockSize = bankAmount * bankSize    // 每块字节数
//  val bankAmount = blockSize / bankSize // 每块bank数
  val tagLen = 32 - indexLen - log2Ceil(blockSize) // tag宽度

  val io = IO(new Bundle {
    val busData = Input(new ValidBundle)

    val flush = Input(Bool())
    val addr = Input(new ValidBundle)
    val inst = Output(UInt(dataLen.W))
    val hit = Output(Bool())
    val miss = Output(Bool())
  })

  val sIdle :: sMiss :: Nil = Enum(2)
  val state = RegInit(sIdle)
  val cnt = RegInit(0.U((bankAmount + 1).W))
  val miss = RegInit(false.B)

  /** valid(way)(index) */
  val valid = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(depth)(false.B)))))
  /** LRU(index) */
  val LRU = RegInit(VecInit(Seq.fill(depth)(0.U((wayAmount-1).W))))

  val tag = io.addr.bits(dataLen-1, dataLen-tagLen)
  val index = io.addr.bits(dataLen-tagLen-1, dataLen-tagLen-indexLen)
  val bankOffset = io.addr.bits(log2Ceil(blockSize)-1, log2Ceil(bankSize))

  /** Write enable, we(way)(bank) */
  val we = Wire(Vec(wayAmount, Vec(bankAmount, Bool())))
  /** Tag write enable, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))
  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))
  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  for(i <- 0 until wayAmount){
    val writing = state === sMiss && !cnt(bankAmount) && io.busData.valid
    when(writing && LRU(index) === i.U){
      for(j <- 0 until bankAmount) {
        we(i)(j) := cnt(j)
      }
    }.otherwise {
      we(i) := 0.U.asTypeOf(Vec(bankAmount, Bool()))
    }
    tagWe(i) := state === sMiss && cnt(0) && io.busData.valid && LRU(index) === i.U
  }

  /** If any one of way is valid and tag match */
  val tagMatch = (0 until wayAmount).foldLeft(false.B)((r, e) => {r || (valid(e)(index) && tagData(e) === tag)})
  val hit = !io.flush && state === sIdle && tagMatch || io.addr.bits(1,0) =/= 0.U
  io.hit := hit
  io.miss := miss

  val hitWay = Wire(UInt(log2Ceil(wayAmount).W))
  hitWay := 0.U
  for(i <- 0 until wayAmount){
    when(valid(i)(index) && tagData(i) === tag) {
      hitWay := i.U
    }
  }

//  io.inst := bankData(RegNext(hitWay))(io.bankSel)
  io.inst := bankData(RegNext(hitWay))(RegNext(bankOffset))

  when(io.flush){
    miss := false.B
    cnt := 0.U
    state := sIdle
  }.otherwise{
    switch(state){
      is(sIdle){
        when(io.addr.valid){
          miss := !tagMatch
          state := Mux(tagMatch, sIdle, sMiss)
          when(tagMatch) {
            LRU(index) := !hitWay.asBool()
          }.otherwise {
            cnt := 1.U
          }
        }
      }
      is(sMiss){
        when(!cnt(bankAmount) && io.busData.valid){
          cnt := cnt << 1
        }.elsewhen(cnt(bankAmount)){
          valid(LRU(index))(index) := true.B
          cnt := 0.U
          state := sIdle
          miss := false.B
        }
      }
    }
  }
  val instBanks = for(i <- 0 until wayAmount; j <- 0 until bankAmount) yield {
    val bank = Module(new SinglePortBank(depth, dataLen, syncRead = true))
    bank.io.we := we(i)(j)
    bank.io.en.get := true.B
    bank.io.addr := index
    bank.io.inData := io.busData.bits
    bankData(i)(j) := bank.io.outData
  }
  val tagBanks = for(i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(depth, tagLen, syncRead = false))
    bank.io.we := tagWe(i)
    bank.io.addr := index
    bank.io.inData := tag
    tagData(i) := bank.io.outData
  }
}
