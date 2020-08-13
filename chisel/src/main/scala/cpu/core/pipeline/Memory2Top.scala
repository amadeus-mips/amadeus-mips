package cpu.core.pipeline

import chisel3._
import chisel3.util._
import cpu.CPUConfig
import cpu.core.Constants._
import cpu.core.bundles.stages.{Mem1Mem2Bundle, Mem2WbBundle}
import shared.Util

class Memory2Top(implicit conf: CPUConfig) extends Module {
  val io = IO(new Bundle() {
    val ins          = Input(Vec(conf.decodeWidth, new Mem1Mem2Bundle))
    val uncachedData = Input(UInt(dataLen.W))
    val cachedData   = Input(UInt(dataLen.W))
    val out          = Output(Vec(conf.decodeWidth, new Mem2WbBundle))
  })
  val memSlot  = Mux(opIsLoad(io.ins(0).op), 0.U, 1.U)
  val readData = Mux(io.ins(memSlot).uncached, io.uncachedData, io.cachedData)

  io.out.zip(io.ins).foreach {
    case (out, in) =>
      out.op    := in.op
      out.write := in.write
      assert(in.valid || !in.pc.orR())
      out.pc := in.pc
      when(!in.valid) {
        out.write.enable := false.B
        out.pc           := 0.U
      }
  }

  when(io.ins(memSlot).valid) {
    when(opIsLoad(io.ins(memSlot).op)) {
      io.out(memSlot).write.valid := true.B
      io.out(memSlot).write.data := MuxLookup(
        io.ins(memSlot).op,
        io.ins(memSlot).write.data,
        Seq(
          MEM_LB -> MuxLookup(
            io.ins(memSlot).addrL2,
            io.ins(memSlot).write.data,
            Seq(
              "b11".U -> Util.signedExtend(readData(31, 24)),
              "b10".U -> Util.signedExtend(readData(23, 16)),
              "b01".U -> Util.signedExtend(readData(15, 8)),
              "b00".U -> Util.signedExtend(readData(7, 0))
            )
          ),
          MEM_LBU -> MuxLookup(
            io.ins(memSlot).addrL2,
            io.ins(memSlot).write.data,
            Seq(
              "b11".U -> Util.zeroExtend(readData(31, 24)),
              "b10".U -> Util.zeroExtend(readData(23, 16)),
              "b01".U -> Util.zeroExtend(readData(15, 8)),
              "b00".U -> Util.zeroExtend(readData(7, 0))
            )
          ),
          MEM_LH -> Mux(
            io.ins(memSlot).addrL2(1),
            Util.signedExtend(readData(31, 16)),
            Util.signedExtend(readData(15, 0))
          ),
          MEM_LHU -> Mux(
            io.ins(memSlot).addrL2(1),
            Util.zeroExtend(readData(31, 16)),
            Util.zeroExtend(readData(15, 0))
          ),
          MEM_LW -> readData,
          MEM_LWL -> MuxLookup(
            io.ins(memSlot).addrL2,
            io.ins(memSlot).write.data,
            Seq(
              "b11".U -> readData,
              "b10".U -> Cat(readData(23, 0), io.ins(memSlot).write.data(7, 0)),
              "b01".U -> Cat(readData(15, 0), io.ins(memSlot).write.data(15, 0)),
              "b00".U -> Cat(readData(7, 0), io.ins(memSlot).write.data(23, 0))
            )
          ),
          MEM_LWR -> MuxLookup(
            io.ins(memSlot).addrL2,
            io.ins(memSlot).write.data,
            Seq(
              "b11".U -> Cat(io.ins(memSlot).write.data(31, 8), readData(31, 24)),
              "b10".U -> Cat(io.ins(memSlot).write.data(31, 16), readData(31, 16)),
              "b01".U -> Cat(io.ins(memSlot).write.data(31, 24), readData(31, 8)),
              "b00".U -> readData
            )
          )
        )
      )
    }
  }
}
