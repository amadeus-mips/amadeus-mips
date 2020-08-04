package cpu.core.components

import chisel3._
import chisel3.util.{MuxLookup, Valid, Cat}
import cpu.CPUConfig
import cpu.core.Constants._

class BrPrUpdateBundle(implicit c: CPUConfig) extends Bundle {
  val jump = Bool()
  val pc   = UInt(addrLen.W)
  val history = UInt(c.branchPredictorHistoryLen.W)

  override def cloneType: BrPrUpdateBundle.this.type = new BrPrUpdateBundle().asInstanceOf[this.type ]
}

abstract class BaseBranchPredictor(implicit c: CPUConfig) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(addrLen.W))

    val update = Flipped(Valid(new BrPrUpdateBundle))

    val prediction = Output(Vec(c.fetchAmount, Bool()))
    val history = Output(UInt(c.branchPredictorHistoryLen.W))
  })
}

class TwoBitPredictor(implicit val conf: CPUConfig) extends BaseBranchPredictor() {
  val len = conf.branchPredictorAddrLen
  val historyLen = conf.branchPredictorHistoryLen

  /** predicTable[history][addr] */
  val predictTable = RegInit(VecInit(Seq.fill(1 << (len - 2))("b10".U(2.W))))

  val history = RegInit(0.U(historyLen.W))

  val update = RegNext(io.update)

  val current = predictTable(update.bits.pc(len - 1, 2) ^ update.bits.history)
  when(update.valid) {
    history := Cat(history(historyLen-2, 0), update.bits.jump)
    predictTable(update.bits.pc(len - 1, 2) ^ update.bits.history) := MuxLookup(
      current,
      current,
      Seq(
        0.U -> Mux(update.bits.jump, 1.U, 0.U),
        1.U -> Mux(update.bits.jump, 2.U, 0.U),
        2.U -> Mux(update.bits.jump, 3.U, 1.U),
        3.U -> Mux(update.bits.jump, 3.U, 2.U)
      )
    )
  }

  val predictionEntries = (0 until conf.fetchAmount).map(i => io.pc + (i * 4).U).map(pc => predictTable(pc(len - 1, 2) ^ history))

  io.prediction := VecInit(predictionEntries.map(bits => bits(1)))
  io.history    := history
}
