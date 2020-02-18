package cpu
import cpu.testing.{CPUFlatSpec, CPUTestDriver, InstructionTests}

class PipeLinedCPUBEQTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  var testName = 0
  for (test <- InstructionTests.branchType) {
    it should s"run ${test.directoryName}/${test.memFile}:$testName" in {
      //TODO: make pipelined an argument
      CPUTestDriver("pipelined", test) should be(true)
    }
    testName = testName + 1
  }
}
