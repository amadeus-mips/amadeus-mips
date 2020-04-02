package testers

import testSuite.{CPUTestDriver, InstructionTests}

object SingleTest {
  def main(args: Array[String]): Unit = {
    require(
      args.length == 2,
      "Error: Expected at least three argument: cpu type, dir name and memFile name\n"
    )

    println(s"Running test ${args(0)}/${args(1)}")

    val test = InstructionTests.nameMap(args(0) + "/" + args(1))

    val testSuccess = CPUTestDriver(test, genVCD = true)
    require(testSuccess, s"the test ${test.directoryName} ${test.memFile} has failed\n")

  }
}
