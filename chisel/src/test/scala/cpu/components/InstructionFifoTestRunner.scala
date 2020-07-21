package cpu.components
import chisel3.iotesters.ChiselFlatSpec
import cpu.CPUConfig
import cpu.core.components.InstructionFIFO

class InstructionFifoTestRunner(dut: InstructionFIFO[chisel3.UInt]) extends InstructionFifoTester(dut) {
  push(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  next()
}

class InstructionFifo2In1Out(dut: InstructionFIFO[chisel3.UInt]) extends InstructionFifoTester(dut) {
  push(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  next()
}

class FifoTestRunner extends ChiselFlatSpec {
  behavior.of("instruction fifo")
  import chisel3._
  implicit val cpuConfig = new CPUConfig(build = false)
  it should "succeed" in {
    iotesters.Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "on"),
      () => new InstructionFIFO[chisel3.UInt](UInt(32.W))
    ) { dut =>
      new InstructionFifoTestRunner(dut)
    } should be(true)
  }
}
