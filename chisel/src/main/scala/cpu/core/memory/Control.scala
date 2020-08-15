// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util.{Cat, Decoupled, Fill, MuxLookup}
import cpu.common.MemReqBundle
import cpu.core.Constants._

class Control extends Module {
  val io = IO(new Bundle {

    /** The data will be write to memory */
    val inMemData = Input(UInt(dataLen.W))
    val operation = Input(UInt(opLen.W))
    val addr      = Input(UInt(dataLen.W))
    val stalled   = Input(Bool())

    /** Whether except happened. From [[cpu.core.memory.Control]] module */
    val except = Input(Bool())

    val request = Decoupled(new MemReqBundle)

    val stallReq = Output(Bool())
  })

  val addrL2  = io.addr(1, 0)
  val cutAddr = Cat(io.addr(dataLen - 1, 2), 0.U(2.W))

  val handShaken = RegInit(false.B)

  when(io.stalled && io.request.fire() && !handShaken) {
    handShaken := true.B
  }.elsewhen(!io.stalled) {
    handShaken := false.B
  }

  io.request.bits.tag := io.addr(31, 12)
  io.request.bits.physicalIndex := Mux(
    VecInit(MEM_LWL, MEM_LWR, MEM_SWL, MEM_SWR).contains(io.operation),
    Cat(io.addr(11, 2), 0.U(2.W)),
    io.addr(11, 0)
  )
  io.request.valid := !io.except && (opIsLoad(io.operation) || opIsStore(io.operation)) && !handShaken
  io.request.bits.writeMask := MuxLookup(
    io.operation,
    0.U,
    Array(
      MEM_SB -> MuxLookup(
        addrL2,
        0.U,
        Array(
          "b11".U -> "b1000".U,
          "b10".U -> "b0100".U,
          "b01".U -> "b0010".U,
          "b00".U -> "b0001".U
        )
      ),
      MEM_SH -> Mux(addrL2(1), "b1100".U, "b0011".U),
      MEM_SW -> "b1111".U,
      MEM_SWL -> MuxLookup(
        addrL2,
        0.U,
        Seq(
          "b11".U -> "b1111".U,
          "b10".U -> "b0111".U,
          "b01".U -> "b0011".U,
          "b00".U -> "b0001".U
        )
      ),
      MEM_SWR -> MuxLookup(
        addrL2,
        0.U,
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b1100".U,
          "b01".U -> "b1110".U,
          "b00".U -> "b1111".U
        )
      )
    )
  )
  io.request.bits.writeData := MuxLookup(
    io.operation,
    io.inMemData, // default SW
    Array(
      MEM_SB -> Fill(4, io.inMemData(7, 0)),
      MEM_SH -> Fill(2, io.inMemData(15, 0)),
      MEM_SWL -> MuxLookup(
        addrL2,
        0.U,
        Seq(
          "b11".U -> io.inMemData,
          "b10".U -> Cat(0.U(8.W), io.inMemData(31, 8)),
          "b01".U -> Cat(0.U(16.W), io.inMemData(31, 16)),
          "b00".U -> Cat(0.U(24.W), io.inMemData(31, 24))
        )
      ),
      MEM_SWR -> MuxLookup(
        addrL2,
        0.U,
        Seq(
          "b11".U -> Cat(io.inMemData(7,0), 0.U(24.W)),
          "b10".U -> Cat(io.inMemData(15,0), 0.U(16.W)),
          "b01".U -> Cat(io.inMemData(23,0), 0.U(8.W)),
          "b00".U -> io.inMemData
        )
      ),
    )
  )

  io.stallReq := io.request.valid && !io.request.ready
}
