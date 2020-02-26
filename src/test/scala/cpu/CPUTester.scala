package cpu
import cpu.testing.{CPUFlatSpec, CPUTestDriver, InstructionTests}

class PipeLinedCPUBRTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.branchType) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      //TODO: make pipelined an argument
      CPUTestDriver("pipelined", test) should be(true)
    }
  }
}

class PipeLinedCPUALLTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for ((group, tests) <- InstructionTests.tests) {
    for (test <- tests) {
      it should s"run group:$group test ${test.directoryName}/${test.memFile}" in {
        //TODO: make pipelined an argument
        CPUTestDriver("pipelined", test) should be(true)
      }
    }
  }
}

class PipelinedCPURTypeTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.rtype) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver("pipelined", test) should be(true)
    }
  }
}

class PipelinedCPUJTypeTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.jtype) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver("pipelined", test) should be(true)
    }
  }
}

class PipelinedCPUHazardTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.hazardTest) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver("pipelined", test) should be(true)
    }
  }
}
