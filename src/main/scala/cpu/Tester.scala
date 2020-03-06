package cpu

import chisel3.iotesters.TesterOptionsManager
import cpu.simulate.build
import treadle.TreadleTester

object Tester {
  def main(args: Array[String]): Unit = {
    // fixed the use of copy here.
    val optionsManager = new TesterOptionsManager {
      treadleOptions = treadleOptions.copy(callResetAtStartUp = true)
    }
    optionsManager.setTargetDirName("./simulator_run_dir")
    val cpuType = args(0)
    val directoryName = args(1)
    val memFile = args(2)

    val hexName = s"./testMemFile/$directoryName/$memFile.txt"

    println(s"hex name is ${hexName}")
    // Create the CPU config. This sets the type of CPU and the binary to load
    val conf = new CPUConfig()
    conf.cpuType = cpuType
    conf.memFile = hexName

    val compiledFirrtl = build(optionsManager, conf)
    val simulator = TreadleTester(compiledFirrtl, optionsManager)
//    var done = false

    // example of how to look at treadle's view of the symbol table
    simulator.engine.symbolTable.nameToSymbol.keys.toSeq.sorted.foreach { key => println(s"symbol: $key") }
    println(s"sr is ${simulator.peek("cpu.cpZero.regSR")}")
//    while (!done) {
//      simulator.step(1)
//      if (simulator.peek("counter") == 14) {
//        done = true
//      }
//    }
  }
}
