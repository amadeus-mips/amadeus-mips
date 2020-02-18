package cpu

import cpu.testing.{CPUTestDriver, InstructionTests}
import treadle.TreadleOptionsManager
import treadle.repl.HasReplConfig

/** Wrapper object to run the treadle REPL for debugging CPU designs
  * Use main function to call
  */
object REPLRunner {
  val helptext = "usage: replrunner <test name> <CPU type>"

  /** Runs a test in an interactive environment allows single steping and reading register values
    * params test_name CPU_type
    * example usage
    * {{{
    * runMain replrunner add1 pipelined
    * }}}
    */
  def main(args: Array[String]): Unit = {
    require(args.length >= 2, "Error: Expected at least two argument\n" + helptext)

    println(s"Running test ${args(1)} on CPU design ${args(0)}")

    //get the params, test name and cpu type
    val test = InstructionTests.nameMap(args(1))
    val cpuType = args(0)

    //setup test
    val driver = new CPUTestDriver(cpuType, test.directoryName, test.memFile)
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    //create a copy of the options from the testerdriver, with the replconfig option
    val options = new TreadleOptionsManager with HasReplConfig
    options.treadleOptions = driver.optionsManager.treadleOptions.copy()
    options.firrtlOptions = driver.optionsManager.firrtlOptions.copy()

    //copy the configured simulator in the REPL
    val repl = treadle.TreadleRepl(options.toAnnotationSeq)
    repl.currentTreadleTesterOpt = Some(driver.simulator)

    repl.run()

    //check test result
    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }
  }
}
