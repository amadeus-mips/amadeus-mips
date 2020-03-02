// See README.md for license details.

package cpu.cache

import chisel3._
import chisel3.util.{isPow2, log2Ceil}

class SimpleDualPortMaskBank(depth: Int, maskW: Int, maskN: Int, syncRead: Boolean = false) extends Module {
  require(isPow2(depth))
  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val rAddr = Input(UInt(log2Ceil(depth).W))
    val we = Input(Bool())
    val mask = Input(UInt(maskN.W))
    val wAddr = Input(UInt(log2Ceil(depth).W))
    val inData = Input(UInt((maskW*maskN).W))
    val outData = Output(UInt((maskW*maskN).W))
  })
  val maskBankUnit = Module(new SyncReadMaskBankUnit(depth, maskW, maskN, syncRead))
  maskBankUnit.io.rAddr := io.rAddr
  maskBankUnit.io.we := io.we
  maskBankUnit.io.mask <> io.mask.asBools()
  maskBankUnit.io.wAddr := io.wAddr
  val inDataVec = Wire(Vec(maskN, UInt(maskW.W)))
  for(n <- 0 until maskN){
    inDataVec(n) := io.inData(maskW * (n + 1) - 1, maskW * n)
  }
  maskBankUnit.io.inData <> inDataVec

//  io.outData := maskBankUnit.io.outData.asUInt()
  io.outData := Mux(!io.we || (io.rAddr =/= io.wAddr),
    maskBankUnit.io.outData.asUInt(),
    {
      val temp = Wire(Vec(maskN, UInt(maskW.W)))
      temp <> maskBankUnit.io.outData
      for ((b, i) <- io.mask.asBools().zipWithIndex) {
        when(b){temp(i) := inDataVec(i)}
      }
      temp.asUInt()
    }
  )

//  printf(p"data = ${Hexadecimal(io.inData)}")
//  printf(p" outData_ = ${Hexadecimal(maskBankUnit.io.outData.asUInt())}\n")
}

class SyncReadMaskBankUnit(depth: Int, maskW: Int, maskN: Int, syncRead: Boolean = false) extends Module {
  require(isPow2(depth))
  val addrLen = log2Ceil(depth)
  val io = IO(new Bundle {
    val rAddr = Input(UInt(addrLen.W))
    val we = Input(Bool())
    val mask = Input(Vec(maskN, Bool()))
    val wAddr = Input(UInt(addrLen.W))
    val inData = Input(Vec(maskN, UInt(maskW.W)))
    val outData = Output(Vec(maskN, UInt(maskW.W)))
  })

  val bank = if(syncRead) SyncReadMem(depth, Vec(maskN, UInt(maskW.W))) else Mem(depth, Vec(maskN, UInt(maskW.W)))
  when(io.we) {
    bank.write(io.wAddr, io.inData, io.mask)
  }
  io.outData := bank.read(io.rAddr)

//  printf(" data_ = ")
//  for(i <- 0 until maskN) {
//    printf(Hexadecimal(io.inData(i)))
//  }
//  printf(p" mask = ${Binary(io.mask.asUInt())}")
//  printf(p" we = ${io.we}")
//  printf(" outData = ")
//  for(i <- 0 until maskN) {
//    printf(Hexadecimal(bank.read(io.rAddr)(i)))
//  }
}
