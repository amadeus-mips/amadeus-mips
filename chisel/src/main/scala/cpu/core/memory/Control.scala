// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util.{DecoupledIO, Fill, MuxLookup, ValidIO}
import common.Util
import cpu.common.{DataReadIO, DataWriteIO}
import cpu.core.Constants._

class Control extends Module {
  val io = IO(new Bundle {
    val inWriteData = Input(UInt(dataLen.W)) // The data write to regfile
    val inMemData = Input(UInt(dataLen.W)) // The data write to memory
    val operation = Input(UInt(opLen.W))
    val addrL2 = Input(UInt(2.W))

    /** Whether except happened. From `Except` module */
    val except = Input(Bool())

    val load = new DataReadIO
    val store = new DataWriteIO

    val outWriteData = Output(UInt(dataLen.W)) // to `WriteBack` Module
    val stallReq = Output(Bool())
  })

  io.load.enable := !io.except && opIsLoad(io.operation)
  io.store.enable := !io.except && opIsStore(io.operation)
  io.store.sel := MuxLookup(io.operation, 0.U,
    Array(
      MEM_SB -> MuxLookup(io.addrL2, 0.U,
        Array(
          "b11".U -> "b1000".U,
          "b10".U -> "b0100".U,
          "b01".U -> "b0010".U,
          "b00".U -> "b0001".U
        )
      ),
      MEM_SH -> Mux(io.addrL2(1), "b1100".U, "b0011".U),
      MEM_SW -> "b1111".U
    )
  )
  io.store.data := MuxLookup(io.operation, 0.U,
    Array(
      MEM_SB -> Fill(4, io.inMemData(7, 0)),
      MEM_SH -> Fill(2, io.inMemData(15, 0)),
      MEM_SW -> io.inMemData
    )
  )

  io.outWriteData := MuxLookup(io.operation, io.inWriteData,
    Array(
      MEM_LB -> MuxLookup(io.addrL2, io.inWriteData,
        Array(
          "b11".U -> Util.signedExtend(io.load.data(31, 24)),
          "b10".U -> Util.signedExtend(io.load.data(23, 16)),
          "b01".U -> Util.signedExtend(io.load.data(15, 8)),
          "b00".U -> Util.signedExtend(io.load.data(7, 0)),
        )
      ),
      MEM_LBU -> MuxLookup(io.addrL2, io.inWriteData,
        Array(
          "b11".U -> Util.zeroExtend(io.load.data(31, 24)),
          "b10".U -> Util.zeroExtend(io.load.data(23, 16)),
          "b01".U -> Util.zeroExtend(io.load.data(15, 8)),
          "b00".U -> Util.zeroExtend(io.load.data(7, 0))
        )
      ),
      MEM_LH -> Mux(io.addrL2(1),
        Util.signedExtend(io.load.data(31, 16)),
        Util.signedExtend(io.load.data(15, 0))
      ),
      MEM_LHU -> Mux(io.addrL2(1),
        Util.zeroExtend(io.load.data(31, 16)),
        Util.zeroExtend(io.load.data(15, 0))
      ),
      MEM_LW -> io.load.data
    )
  )
  val loadStall = opIsLoad(io.operation) && !io.load.rValid
  val saveStall = opIsStore(io.operation) && !io.store.bValid
  io.stallReq := !io.except && (loadStall || saveStall)
}
