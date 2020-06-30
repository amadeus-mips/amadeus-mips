package cpu.core.fetch

import chisel3._
import chisel3.util.{Counter, MuxLookup, ValidIO}
import cpu.CPUConfig
import cpu.core.Constants._
import shared.ValidBundle

class BrPrUpdateBundle extends Bundle {
  val taken  = Input(Bool())
  val pc     = Input(UInt(addrLen.W))
  val target = Input(UInt(addrLen.W))
}

class BranchPredictorIO extends Bundle {
  val pc = Input(UInt(addrLen.W))

  val update = Flipped(ValidIO(new BrPrUpdateBundle))

  val prediction = Output(new ValidBundle())
}

abstract class BranchPredictor(val c: CPUConfig, predictBits: Int = 0) extends Module {
  val io = IO(new BranchPredictorIO)

  class BranchPredictorBufferEntry(predictBits: Int) extends Bundle {
    val valid      = Bool()
    val tag        = UInt(c.branchPredictorAddrLen.W)
    val target     = UInt(addrLen.W)
    val statistics = UInt(predictBits.W)

    override def cloneType: BranchPredictorBufferEntry.this.type =
      new BranchPredictorBufferEntry(predictBits).asInstanceOf[this.type]
  }

  val predictTable = RegInit(
    VecInit(Seq.fill(c.branchPredictorTableEntryNum)(0.U.asTypeOf(new BranchPredictorBufferEntry(predictBits))))
  )

  val defaultIndex = Counter(c.branchPredictorTableEntryNum)

  /**
    *
    * @param pc the pc
    * @return index and hit
    */
  def find(pc: UInt): (UInt, Bool) = {
    require(pc.getWidth == addrLen)
    val hitIndex = WireInit(defaultIndex.value)
    val hit      = WireInit(false.B)
    for (i <- 0 until c.branchPredictorTableEntryNum) {
      when(predictTable(i).tag === pc(c.branchPredictorAddrLen - 1, 0)) {
        hitIndex := i.U
        hit      := true.B
      }
    }
    when(!hit) { defaultIndex.inc() }
    (hitIndex, hit)
  }

  def update(f: UInt => UInt): Unit = {
    val (updateIndex, _) = find(io.update.bits.pc)
    when(io.update.valid) {
      predictTable(updateIndex).valid      := true.B
      predictTable(updateIndex).tag        := io.update.bits.pc
      predictTable(updateIndex).target     := io.update.bits.target
      predictTable(updateIndex).statistics := f(predictTable(updateIndex).statistics)
    }
  }
}

abstract class StaticPredictor(c: CPUConfig) extends BranchPredictor(c) {}

abstract class DynamicPredictor(c: CPUConfig, bit: Int) extends BranchPredictor(c, bit) {}

class TwoBitPredictor(implicit val conf: CPUConfig) extends DynamicPredictor(conf, 2) {
  val (index, hit) = find(io.pc)
  io.prediction.valid := hit && predictTable(index).valid && predictTable(index).statistics(1)
  io.prediction.bits  := predictTable(index).target

  update(statistics => {
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
  })

}

class AlwaysTakenPredictor(implicit val conf: CPUConfig) extends StaticPredictor(conf) {
  val (index, hit) = find(io.pc)
  io.prediction.valid := hit && predictTable(index).valid
  io.prediction.bits  := predictTable(index).target

  update(statistics => 0.U)
}

class AlwaysNotTakenPredictor(implicit val conf: CPUConfig) extends StaticPredictor(conf) {
  io.prediction.valid := false.B
  io.prediction.bits := DontCare
}
