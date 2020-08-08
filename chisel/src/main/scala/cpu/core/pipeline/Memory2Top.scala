package cpu.core.pipeline

import chisel3._
import chisel3.util.MuxLookup
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem1Mem2Bundle, Mem2WbBundle}
import shared.Util

class Memory2Top extends Module {
  val io = IO(new Bundle() {
    val in           = Input(new Mem1Mem2Bundle)
    val uncachedData = Input(UInt(dataLen.W))
    val cachedData   = Input(UInt(dataLen.W))
    val out          = Output(new Mem2WbBundle)
  })
  val uncached = io.in.uncached
  val readData = Mux(io.in.uncached, io.uncachedData, io.cachedData)

  io.out.op    := io.in.op
  io.out.write := io.in.write
  io.out.pc    := io.in.pc

  when(io.in.valid) {
    when(opIsLoad(io.in.op)) {
      io.out.write.valid := true.B
      io.out.write.data := MuxLookup(
        io.in.op,
        io.in.write.data,
        Seq(
          MEM_LB -> Mux(
            uncached,
            Util.signedExtend(readData(7, 0)),
            MuxLookup(
              io.in.addrL2,
              io.in.write.data,
              Seq(
                "b11".U -> Util.signedExtend(readData(31, 24)),
                "b10".U -> Util.signedExtend(readData(23, 16)),
                "b01".U -> Util.signedExtend(readData(15, 8)),
                "b00".U -> Util.signedExtend(readData(7, 0))
              )
            )
          ),
          MEM_LBU -> Mux(
            uncached,
            Util.zeroExtend(readData(7, 0)),
            MuxLookup(
              io.in.addrL2,
              io.in.write.data,
              Seq(
                "b11".U -> Util.zeroExtend(readData(31, 24)),
                "b10".U -> Util.zeroExtend(readData(23, 16)),
                "b01".U -> Util.zeroExtend(readData(15, 8)),
                "b00".U -> Util.zeroExtend(readData(7, 0))
              )
            )
          ),
          MEM_LH -> Mux(
            uncached,
            Util.signedExtend(readData(15, 0)),
            Mux(io.in.addrL2(1), Util.signedExtend(readData(31, 16)), Util.signedExtend(readData(15, 0)))
          ),
          MEM_LHU -> Mux(
            uncached,
            Util.zeroExtend(readData(15, 0)),
            Mux(io.in.addrL2(1), Util.zeroExtend(readData(31, 16)), Util.zeroExtend(readData(15, 0)))
          ),
          MEM_LW -> readData
        )
      )
    }
  }
}
