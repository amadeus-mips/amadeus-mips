package cpu

import chisel3._
import chisel3.util._
import firrtl.stage.FirrtlSourceAnnotation
import org.scalatest.{FreeSpec, Matchers}
import treadle.{TreadleOption, TreadleOptionsManager, TreadleTester}
import chisel3.iotesters.TesterOptionsManager
import memory.axi.AXI1x2SramInterface
import memory.physicalMem.{DMemCombinationalPortForAXI, DualPortedCombinMemory, IMemCombinationalPortForAXI}

class TestTop(memFile: String) extends Module {
  val io = IO(new Bundle() {
    val success = Output(Bool())
  })
  io.success := DontCare

  // directory structure for sbt run is at the root directory of the project
  // in this case: ./ = chisel/
  //  val memFile = s"./src/main/scala/zero.txt"

  val cpu = Module(new CPUTop)
  val mem = Module(new DualPortedCombinMemory(1<<16, memFile))
  val imem = Module(new IMemCombinationalPortForAXI)
  val dmem = Module(new DMemCombinationalPortForAXI)
  mem.wireMemAXIPort(imem, dmem)
  val memAXISlave = Module(new AXI1x2SramInterface)

  memAXISlave.io.dram <> dmem.io.axi
  memAXISlave.io.iram <> imem.io.axi
  memAXISlave.io.bus <> cpu.io.bus_axi

  cpu.io.intr := 0.U(6.W)
  cpu.io.debug := DontCare
}

object RegInitProblem {
  def build(optionsManager: TesterOptionsManager): String = {
    optionsManager.firrtlOptions =
      optionsManager.firrtlOptions.copy(compilerName = "low")

    //TODO: delete this after done
    chisel3.Driver.execute(optionsManager, () => new TestTop("./testMemFile/arith/add.txt")) match {
      case ChiselExecutionSuccess(Some(_), _, Some(firrtlExecutionResult)) =>
        firrtlExecutionResult match {
          case firrtl.FirrtlExecutionSuccess(_, compiledFirrtl) =>
            compiledFirrtl
          case firrtl.FirrtlExecutionFailure(message) =>
            throw new Exception(
              s"FirrtlBackend: Compile failed. Message: $message"
            )
        }
      case _ =>
        throw new Exception("Problem with compilation")
    }
  }

  def main(args: Array[String]): Unit = {
    // fixed the use of copy here.
    val optionsManager = new TesterOptionsManager {
      treadleOptions = treadleOptions.copy(callResetAtStartUp = true)
    }
    val compiledFirrtl = build(optionsManager)
    val simulator = TreadleTester(compiledFirrtl, optionsManager)
    var done = false

    // example of how to look at treadle's view of the symbol table
    simulator.engine.symbolTable.nameToSymbol.keys.toSeq.sorted.foreach { key =>
      //      if (key.startsWith("cpu.core.")) {
      println(s"symbol: $key")
      //      }
    }
    //    while (!done) {
    //      simulator.step(1)
    //      if (simulator.peek("cpu.core.fetch.pc") == 8) {
    //        done = true
    //      }
    //    }
    //    assert(done)
  }
}