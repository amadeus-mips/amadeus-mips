package testers

import testSuite.{CPUTestDriver, InstructionTests}
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
    require(
      args.length >= 2,
      "Error: Expected at least three argument: cpu type, test dir name, and test mem file name\n"
    )

    println(s"Running test ${args(0)}/${args(1)} ")

    //get the params, test name and cpu type
    val test = InstructionTests.nameMap(args(0) + "/" + args(1))

    //setup test
    val driver = new CPUTestDriver(args(0), args(1))
    driver.reset()
    // initialize registers in the assembly file

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
