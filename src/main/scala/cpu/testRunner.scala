package cpu
import cpu.testing.{CPUTestDriver, InstructionTests}

object testRunner {
  def main(args: Array[String]): Unit = {
    require(
      args.length >= 3,
      "Error: Expected at least three argument: cpu type, dir name and memFile name\n"
    )

    println(s"Running test ${args(1)}/${args(2)} on CPU design ${args(0)}")

    val test = InstructionTests.nameMap(args(1) + "/" + args(2))
    val cpuType = args(0)

    val driver = new CPUTestDriver(cpuType, args(1), args(2))
    driver.reset()
    driver.initRegs(test.initRegs)
    driver.initMemory(test.initMem)

    driver.step(test.cycles(cpuType))

    if (driver.checkRegs(test.checkRegs) && driver.checkMemory(test.checkMem)) {
      println("Test passed!")
    } else {
      println("Test failed!")
    }
  }
}
