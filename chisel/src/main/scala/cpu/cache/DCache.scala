// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util._
import cpu.common.DefaultConfig._

// TODO move `write back` to the axiWrap
class DCache extends Module {
  val wayAmount = 2   // 每组路数
  val depth = 128     // 组数
  val indexLen = log2Ceil(depth)  // index宽度
  val blockSize = 32*8/8    // 每块字节数
  val bankSize = 32/8       // 每bank字节数
  val bankAmount = blockSize / bankSize // 每块bank数
  val tagLen = 32 - indexLen - log2Ceil(blockSize) // tag宽度
  val maskW = 8             // 最小写入单元的宽度

  val io = IO(new Bundle {
    val bus_rData = Input(UInt(dataLen.W))   // data read from axi bus
    val bus_rValid = Input(Bool())
    val bus_wReady = Input(Bool())           // write to axi bus ready
    val bus_bValid = Input(Bool())           // write to axi bus response valid
    
    // TODO convert to `NiseSramIO`
    val cpu_addr = Input(UInt(addrLen.W))   // address from cpu
    val cpu_ren = Input(Bool())             // read enable from cpu
    val cpu_wen = Input(Bool())             // write enable from cpu
    val cpu_wData = Input(UInt(dataLen.W))      // write data & write enable from cpu
    val cpu_wSel = Input(UInt(bankSize.W))      // write sel from cpu
    val cpu_exeAddr = Input(UInt(addrLen.W))    // execute stage address from cpu

    val cpu_data = Output(UInt(dataLen.W))      // data to cpu
    val hit = Output(Bool())                // read from cache hit
    val wResp = Output(Bool())              // write to cache hit and write to cache succeed
    val miss = Output(Bool())               // cache not hit, need to access memory

    val bus_writeBack = Output(Bool())       // cache replaced item need to write back(dirty)
    val bus_wAddr = Output(UInt(addrLen.W))  // address for write back
    val bus_wData = Output(UInt(dataLen.W))
    val bus_wValid = Output(Bool())
    val bus_wLast = Output(Bool())
  })

  val sIdle :: sWritePre :: sWrite :: sMiss :: Nil = Enum(4)
  val state = RegInit(sIdle)
  val cnt = RegInit(0.U((bankAmount + 1).W))
  val writeCnt = RegInit(0.U((log2Ceil(bankAmount) + 1).W))

  /** valid(way)(index) */
  val valid = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(depth)(false.B)))))
  /** dirty(way)(index) */
  val dirty = RegInit(VecInit(Seq.fill(wayAmount)(VecInit(Seq.fill(depth)(false.B)))))
  /** LRU(index) */
  val LRU = RegInit(VecInit(Seq.fill(depth)(0.U((wayAmount-1).W))))

  val replaceBuffer = RegInit(VecInit(Seq.fill(bankAmount)(0.U(dataLen.W))))

  val tag = io.cpu_addr(dataLen-1, dataLen-tagLen)
  val index = io.cpu_addr(dataLen-tagLen-1, dataLen-tagLen-indexLen)
  val bankOffset = io.cpu_addr(log2Ceil(blockSize)-1, log2Ceil(bankSize))

  /** Write mask, wMask(way)(bank) */
  val wMask = Wire(Vec(wayAmount, Vec(bankAmount, UInt(bankSize.W))))
  /** Tag write enable, tagWe(way) */
  val tagWe = Wire(Vec(wayAmount, Bool()))
  /** Data from every way, bankData(way)(bank) */
  val bankData = Wire(Vec(wayAmount, Vec(bankAmount, UInt(dataLen.W))))
  /** Data from every way tag, tagData(way) */
  val tagData = Wire(Vec(wayAmount, UInt(tagLen.W)))

  val exeChangeLRU = RegInit(0.U((wayAmount-1).W))
  val exeHit = RegInit(false.B)
  val exeWResp = RegInit(false.B)
  val exeData = RegInit(0.U(dataLen.W))

  val bankWData = Mux(state === sIdle, io.cpu_wData, io.bus_rData)
  val exeTag = Mux(state === sIdle, io.cpu_exeAddr(dataLen-1, dataLen-tagLen), tag)
  val exeIndex = Mux(state === sIdle, io.cpu_exeAddr(dataLen-tagLen-1, dataLen-tagLen-indexLen), index)
  val exeBankOffSet = Mux(state === sIdle, io.cpu_exeAddr(log2Ceil(blockSize)-1, log2Ceil(bankSize)), bankOffset)

  val wIndex = index
  val rIndex = Mux(state === sIdle, exeIndex, index)

  for(i <- 0 until wayAmount) {
    for(j <- 0 until bankAmount) {
      wMask(i)(j) := MuxCase(0.U,
        Array(
          (state === sMiss && cnt(j) && io.bus_rValid && LRU(index) === i.U) ->
            Fill(bankSize, true.B),
          (state === sIdle && exeChangeLRU === (1-i).U && io.cpu_wen && exeWResp && bankOffset === j.U) ->
            io.cpu_wSel
        )
      )
    }
    tagWe(i) := state === sMiss && cnt(0) && io.bus_rValid && LRU(index) === i.U
  }

  io.hit := (state === sIdle && io.cpu_ren && exeHit)
  io.cpu_data := exeData
  io.wResp := (state === sIdle && io.cpu_wen && exeWResp)

  /** only for two way cache, and I forgot the meaning of this temp */
  val LRUNotDirty = (!LRU(index) && !dirty(0)(index)) || (LRU(index).asBool() && !dirty(1)(index))
  val LRUDirty = (!LRU(index) && dirty(0)(index)) || (LRU(index).asBool() && dirty(1)(index))
  io.miss :=
      (state === sIdle && (
          (io.cpu_ren && !exeHit && LRUNotDirty) ||
          (io.cpu_wen && !exeWResp && LRUNotDirty)
      )) ||
      (state === sWrite && writeCnt === 8.U && io.bus_bValid) ||
      (state === sMiss)

  val tagIndex = Mux(state === sIdle, exeIndex, index)

  val hitWay = Wire(UInt((log2Ceil(wayAmount) + 1).W))
  val notHit = Fill(log2Ceil(wayAmount) + 1, 1.U)
  hitWay := notHit
  for(i <- 0 until wayAmount){
    when(valid(i)(tagIndex) && tagData(i) === exeTag){
      hitWay := i.U
    }
  }

  exeHit := hitWay =/= notHit
  exeWResp := hitWay =/= notHit
  exeChangeLRU := !hitWay
  exeData := bankData(hitWay)(exeBankOffSet)

  val bus_writeBack = RegInit(false.B)
  val bus_wAddr = RegInit(0.U(addrLen.W))
  val bus_wData = RegInit(0.U(dataLen.W))
  val bus_wValid = RegInit(false.B)
  val bus_wLast = RegInit(false.B)
  io.bus_writeBack := bus_writeBack
  io.bus_wAddr := bus_wAddr
  io.bus_wData := bus_wData
  io.bus_wValid := bus_wValid
  io.bus_wLast := bus_wLast
  switch(state) {
    is(sIdle){
      when(io.cpu_ren){
        when(exeHit){
          LRU(index) := exeChangeLRU
        }.otherwise{
          when(LRUDirty){
            state := sWritePre
          }.otherwise{
            state := sMiss
            cnt := 1.U
            bus_writeBack := false.B
          }
        }
      }.elsewhen(io.cpu_wen){
        when(exeWResp){
          LRU(index) := exeChangeLRU
          dirty(!exeChangeLRU)(index) := true.B
        }.otherwise{
          when(LRUDirty){
            state := sWritePre
          }.otherwise{
            state := sMiss
            cnt := 1.U
            bus_writeBack := false.B
          }
        }
      }
    }
    is(sWritePre){
      val w = LRU(index)
      state := sWrite
      cnt := 0.U
      writeCnt := 0.U
      for(i <- 1 until bankAmount){
        replaceBuffer(i) := bankData(w)(i)
      }
      bus_wAddr := Cat(tagData(w), index, 0.U(5.W))
      bus_wData := bankData(w)(0)
      bus_wValid := true.B
      bus_wLast := false.B
      bus_writeBack := true.B
    }
    is(sWrite){
      when(writeCnt =/= 8.U && io.bus_wReady){
        writeCnt := writeCnt + 1.U
        bus_wData := replaceBuffer(writeCnt + 1.U)
        when(writeCnt === 6.U) {
          bus_wLast := true.B
        }
        when(writeCnt === 7.U){
          bus_wValid := false.B
          bus_writeBack := false.B
        }
      }.elsewhen(writeCnt === 8.U && io.bus_bValid){
        state := sMiss
        cnt := 1.U
        writeCnt := 0.U
        bus_writeBack := false.B
      }
    }
    is(sMiss){
      when(!cnt(bankAmount) && io.bus_rValid){
        cnt := cnt << 1
        when(cnt(6)){
          dirty(LRU(index))(index) := false.B
          valid(LRU(index))(index) := true.B
        }
      }.elsewhen(cnt(bankAmount)) {
        cnt := 0.U
        state := sIdle
      }
    }
  }
  val tagBanks = for(i <- 0 until wayAmount) yield {
    val bank = Module(new SinglePortBank(depth, tagLen, syncRead = false, true))
    bank.io.we := tagWe(i)
    bank.io.addr := tagIndex
    bank.io.inData := tag
    tagData(i) := bank.io.outData
  }
  val dataBanks = for(i <- 0 until wayAmount; j <- 0 until bankAmount) yield {
    val bank = Module(new SimpleDualPortMaskBank(depth, maskW, maskN = bankSize))
    bank.io.rAddr := rIndex
    bank.io.we := wMask(i)(j) =/= 0.U
    bank.io.mask := wMask(i)(j)
    bank.io.wAddr := wIndex
    bank.io.inData := bankWData
    bankData(i)(j) := bank.io.outData
  }
}
