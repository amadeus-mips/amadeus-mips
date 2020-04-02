package cpu

import testSuite.{CPUFlatSpec, CPUTestDriver, InstructionTests}

// Don't generate VCD dump in batch testing

class PipeLinedCPUBRTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.branchType) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      //TODO: make pipelined an argument
      CPUTestDriver(test, genVCD = false) should be(true)
    }
  }
}

class PipeLinedCPUALLTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for ((group, tests) <- InstructionTests.tests) {
    for (test <- tests) {
      it should s"run group:$group test ${test.directoryName}/${test.memFile}" in {
        CPUTestDriver(test, genVCD = false) should be(true)
      }
    }
  }
}

class PipelinedCPURTypeTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.rtype) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver(test, genVCD = false) should be(true)
    }
  }
}

class PipelinedCPUJTypeTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.jtype) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver(test, genVCD = false) should be(true)
    }
  }
}

class PipelinedCPUHazardTester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.hazardTest) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver(test, genVCD = false) should be(true)
    }
  }
}

class PipelinedCPUCP0Tester extends CPUFlatSpec {
  behavior.of("pipelined CPU")
  for (test <- InstructionTests.cp0Tests) {
    it should s"run ${test.directoryName}/${test.memFile}" in {
      CPUTestDriver(test, genVCD = false) should be(true)
    }
  }
}
