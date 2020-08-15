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
    // this commit sync with data
    val uncachedCommit = Input(Bool())
    val cachedCommit = Input(Bool())
    val uncachedData = Input(UInt(dataLen.W))
    val cachedData   = Input(UInt(dataLen.W))
    val out          = Output(Vec(conf.decodeWidth, new Mem2WbBundle))
  })
  val memSlot  = Mux(opIsLoad(io.ins(0).op), 0.U, 1.U)
  val readData = WireInit(Mux(io.ins(memSlot).uncached, io.uncachedData, io.cachedData))

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

  val uncachedCommitBuffer = RegInit(false.B)
  val cachedCommitBuffer = RegInit(false.B)
  val uncachedDataBuffer = RegInit(0.U(32.W))
  val cachedDataBuffer = RegInit(0.U(32.W))

  when(!opIsLoad(io.ins(memSlot).op)) {
    when(io.uncachedCommit) {
      uncachedCommitBuffer := true.B
      uncachedDataBuffer := io.uncachedData
    }
    when(io.cachedCommit) {
      cachedCommitBuffer := true.B
      cachedDataBuffer := io.cachedData
    }
  }.elsewhen(!io.ins(memSlot).valid){
    uncachedCommitBuffer := false.B
    cachedCommitBuffer := false.B
  }.otherwise{
    when(io.ins(memSlot).uncached) {
      when(io.cachedCommit) {
        cachedCommitBuffer := true.B
        cachedDataBuffer := io.cachedData
      }
      when(io.uncachedCommit){
        assert(!uncachedCommitBuffer)
        readData := io.uncachedData
      }.otherwise {
        assert(uncachedCommitBuffer)
        readData := uncachedDataBuffer
        uncachedCommitBuffer := false.B
      }
    }.otherwise{
      when(io.uncachedCommit){
        uncachedCommitBuffer := true.B
        uncachedDataBuffer := io.uncachedData
      }
      when(io.cachedCommit) {
        assert(!cachedCommitBuffer)
        readData := io.cachedData
      }.otherwise{
        assert(cachedCommitBuffer)
        readData := cachedDataBuffer
        cachedCommitBuffer := false.B
      }
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
          MEM_LL -> readData,
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
