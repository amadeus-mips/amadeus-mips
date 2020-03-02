// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util.{Cat, Fill, MuxLookup}
import common.Util
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import cpu.core.Constants._

class Control extends Module {
  val io = IO(new Bundle {
    val inWriteData = Input(UInt(dataLen.W)) // The data write to regfile
    val inMemData = Input(UInt(dataLen.W)) // The data write to memory
    val operation = Input(UInt(opLen.W))
    val addr = Input(UInt(dataLen.W))

    /** Whether except happened. From `Except` module */
    val except = Input(Bool())

    val rData = new NiseSramReadIO
    val wData = new NiseSramWriteIO

    val outWriteData = Output(UInt(dataLen.W)) // to `WriteBack` Module
    val stallReq = Output(Bool())
  })

  val addrL2 = io.addr(1,0)
  val cutAddr = Cat(io.addr(dataLen-1, 2), 0.U(2.W))

  io.rData.addr := cutAddr
  io.rData.enable := !io.except && opIsLoad(io.operation)
  io.wData.addr := cutAddr
  io.wData.enable := !io.except && opIsStore(io.operation)
  io.wData.sel := MuxLookup(io.operation, 0.U,
    Array(
      MEM_SB -> MuxLookup(addrL2, 0.U,
        Array(
          "b11".U -> "b1000".U,
          "b10".U -> "b0100".U,
          "b01".U -> "b0010".U,
          "b00".U -> "b0001".U
        )
      ),
      MEM_SH -> Mux(addrL2(1), "b1100".U, "b0011".U),
      MEM_SW -> "b1111".U
    )
  )
  io.wData.data := MuxLookup(io.operation, 0.U,
    Array(
      MEM_SB -> Fill(4, io.inMemData(7, 0)),
      MEM_SH -> Fill(2, io.inMemData(15, 0)),
      MEM_SW -> io.inMemData
    )
  )

  io.outWriteData := MuxLookup(io.operation, io.inWriteData,
    Array(
      MEM_LB -> MuxLookup(addrL2, io.inWriteData,
        Array(
          "b11".U -> Util.signedExtend(io.rData.data(31, 24)),
          "b10".U -> Util.signedExtend(io.rData.data(23, 16)),
          "b01".U -> Util.signedExtend(io.rData.data(15, 8)),
          "b00".U -> Util.signedExtend(io.rData.data(7, 0)),
        )
      ),
      MEM_LBU -> MuxLookup(addrL2, io.inWriteData,
        Array(
          "b11".U -> Util.zeroExtend(io.rData.data(31, 24)),
          "b10".U -> Util.zeroExtend(io.rData.data(23, 16)),
          "b01".U -> Util.zeroExtend(io.rData.data(15, 8)),
          "b00".U -> Util.zeroExtend(io.rData.data(7, 0))
        )
      ),
      MEM_LH -> Mux(addrL2(1),
        Util.signedExtend(io.rData.data(31, 16)),
        Util.signedExtend(io.rData.data(15, 0))
      ),
      MEM_LHU -> Mux(addrL2(1),
        Util.zeroExtend(io.rData.data(31, 16)),
        Util.zeroExtend(io.rData.data(15, 0))
      ),
      MEM_LW -> io.rData.data
    )
  )
  val loadStall = opIsLoad(io.operation) && !io.rData.valid
  val saveStall = opIsStore(io.operation) && !io.wData.valid
  io.stallReq := !io.except && (loadStall || saveStall)
}
