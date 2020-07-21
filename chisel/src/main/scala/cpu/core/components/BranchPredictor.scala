package cpu.core.components

import chisel3._
import chisel3.util.{MuxLookup, Valid}
import cpu.BranchPredictorType.TwoBit
import cpu.core.Constants._
import cpu.{BranchPredictorType, CPUConfig}
import shared.ValidBundle

class BrPrUpdateBundle extends Bundle {
  val taken  = Bool()
  val pc     = UInt(addrLen.W)
  val target = UInt(addrLen.W)
}

class BranchPredictorIO(fa: Int, predType: BranchPredictorType) extends Bundle {
  val pc = Input(UInt(addrLen.W))

  val update = Flipped(Valid(new BrPrUpdateBundle))

  val prediction = Output(Vec(fa, new BranchPredictorEntryBundle(predType)))
}

class BranchPredictorEntryBundle(predType: BranchPredictorType) extends Bundle {
  val target = UInt(addrLen.W)
  val statistics = UInt(predType match {
    case TwoBit => 2.W
    case _      => 0.W
  })
  val valid = Bool()

  override def cloneType: BranchPredictorEntryBundle.this.type =
    new BranchPredictorEntryBundle(predType).asInstanceOf[this.type]
}

abstract class BranchPredictor(val c: CPUConfig) extends Module {
  val io = IO(new BranchPredictorIO(c.fetchAmount, c.branchPredictorType))

  class BranchPredictorEntry(predType: BranchPredictorType) extends Bundle {
    val target = UInt(addrLen.W)
    val statistics = UInt(predType match {
      case TwoBit => 2.W
      case _      => 0.W
    })

    override def cloneType: BranchPredictorEntry.this.type =
      new BranchPredictorEntry(predType).asInstanceOf[this.type]
  }

  val predictTable      = Mem(1 << (c.branchPredictorAddrLen - 2), new BranchPredictorEntry(c.branchPredictorType))
  val predictTableValid = RegInit(VecInit(Seq.fill(1 << (c.branchPredictorAddrLen - 2))(false.B)))

  def get(pc: UInt): (BranchPredictorEntry, Bool) = {
    require(pc.getWidth == addrLen)
    (predictTable.read(pc), predictTableValid(pc(c.branchPredictorAddrLen - 1, 2)))
  }
}

abstract class StaticPredictor(c: CPUConfig) extends BranchPredictor(c) {}

abstract class DynamicPredictor(c: CPUConfig) extends BranchPredictor(c) {
  def update(statistics: UInt): UInt

  val predictionEntries = (0 until c.fetchAmount).map(e => io.pc + (e * 4).U).map(get)

  io.prediction
    .zip(predictionEntries)
    .foreach(e => {
      val (o, (entry, valid)) = e
      o.valid := valid
      o.target  := entry.target
      o.statistics := entry.statistics
    })

  val updateEntry = Wire(new BranchPredictorEntry(c.branchPredictorType))
  updateEntry.target     := io.update.bits.target
  updateEntry.statistics := update(predictTable.read(io.update.bits.pc).statistics)
  when(io.update.valid) {
    predictTable.write(io.update.bits.pc, updateEntry)
    predictTableValid(io.update.bits.pc(c.branchPredictorAddrLen - 1, 2)) := true.B
  }
}

class TwoBitPredictor(implicit val conf: CPUConfig) extends DynamicPredictor(conf) {
  override def update(statistics: UInt): UInt = {
    MuxLookup(
      statistics,
      statistics,
      Seq(
        0.U -> Mux(io.update.bits.taken, 1.U, 0.U),
        1.U -> Mux(io.update.bits.taken, 2.U, 0.U),
        2.U -> Mux(io.update.bits.taken, 3.U, 1.U),
        3.U -> Mux(io.update.bits.taken, 3.U, 2.U)
      )
    )
  }
}

class AlwaysTakenPredictor(implicit val conf: CPUConfig) extends DynamicPredictor(conf) {
  override def update(statistics: UInt): UInt = 0.U
}

class AlwaysNotTakenPredictor(implicit val conf: CPUConfig) extends StaticPredictor(conf) {
  io.prediction := 0.U
}
