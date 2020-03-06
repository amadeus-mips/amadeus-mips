package cpu.utils

import chisel3._
import firrtl.stage.FirrtlSourceAnnotation
import treadle.repl.HasReplConfig
import treadle.{TreadleOptionsManager, TreadleTester}

class foo extends Module {
  val io = IO(new Bundle() {
    val en = Input(Bool())
    val data = Input(UInt(3.W))
    val output = Output(UInt(3.W))
  })
  val fooReg = RegInit(5.U(3.W))
  when(io.en) {
    fooReg := fooReg + 1.U
    io.output := fooReg
  }.otherwise {
    io.output := fooReg
  }

}

object YourModuleDriver extends App {

  val optionsManager = new TreadleOptionsManager
  val rtl = build(optionsManager)
  val sourceAnnotation = FirrtlSourceAnnotation(rtl)
  val simulator = TreadleTester(sourceAnnotation +: optionsManager.toAnnotationSeq)
  val options = new TreadleOptionsManager with HasReplConfig
  val repl = treadle.TreadleRepl(options.toAnnotationSeq)

  def build(optionsManager: TreadleOptionsManager): String = {

    chisel3.Driver.execute(args, () => new foo) match {
      case ChiselExecutionSuccess(Some(_), _, Some(firrtlExecutionResult)) =>
        firrtlExecutionResult match {
          case firrtl.FirrtlExecutionSuccess(_, compiledFirrtl) =>
            compiledFirrtl
          case firrtl.FirrtlExecutionFailure(message) =>
            throw new Exception(s"FirrtlBackend: Compile failed. Message: $message")
        }
      case _ =>
        throw new Exception("Problem with compilation")
    }
  }
  repl.currentTreadleTesterOpt = Some(simulator)
  repl.run()
}
