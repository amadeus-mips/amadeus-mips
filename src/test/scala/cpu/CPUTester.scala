package cpu
import cpu.testing.{CPUFlatSpec, CPUTestDriver, InstructionTests}

class PipeLinedCPUBEQTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.branchType) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      //TODO: make pipelined an argument
      CPUTestDriver("pipelined", test) should be(true)
    }
  }
}
