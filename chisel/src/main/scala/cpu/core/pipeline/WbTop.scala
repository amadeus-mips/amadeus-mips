package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxLookup
import cpu.core.Constants._
import cpu.core.bundles.stages.MemWbBundle
import shared.Util

class WbTop extends Module {
  val io = IO(new Bundle() {
    val in = Input(new MemWbBundle)
    val rData = Input(UInt(dataLen.W))
    val out = Output(new MemWbBundle)
  })

  io.out := io.in
  when(opIsLoad(io.in.operation)) {
    io.out.write.valid := true.B
    io.out.write.data := MuxLookup(
      io.in.operation,
      io.in.write.data,
      Array(
        MEM_LB -> MuxLookup(
          io.in.addrL2,
          io.in.write.data,
          Array(
            "b11".U -> Util.signedExtend(io.rData(31, 24)),
            "b10".U -> Util.signedExtend(io.rData(23, 16)),
            "b01".U -> Util.signedExtend(io.rData(15, 8)),
            "b00".U -> Util.signedExtend(io.rData(7, 0))
          )
        ),
        MEM_LBU -> MuxLookup(
          io.in.addrL2,
          io.in.write.data,
          Array(
            "b11".U -> Util.zeroExtend(io.rData(31, 24)),
            "b10".U -> Util.zeroExtend(io.rData(23, 16)),
            "b01".U -> Util.zeroExtend(io.rData(15, 8)),
            "b00".U -> Util.zeroExtend(io.rData(7, 0))
          )
        ),
        MEM_LH  -> Mux(io.in.addrL2(1), Util.signedExtend(io.rData(31, 16)), Util.signedExtend(io.rData(15, 0))),
        MEM_LHU -> Mux(io.in.addrL2(1), Util.zeroExtend(io.rData(31, 16)), Util.zeroExtend(io.rData(15, 0))),
        MEM_LW  -> io.rData
      )
    )
  }
}
