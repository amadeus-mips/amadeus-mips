package cpu.cache.dataCache

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import org.scalatest.Matchers

class PassThroughModule extends chisel3.Module {
  import chisel3._
  val io = IO(new Bundle {
    val in  = Input(Bool())
    val out = Output(Bool())
  })
  io.out := RegNext(io.in)
}
class PassThroughTest(dut: PassThroughModule) extends PeekPokeTester(dut) {
  poke(dut.io.in, 0)
  step(1)
  for (i <- 0 until 5) {
    poke(dut.io.in, i % 2 == 0)
    expect(dut.io.out, i % 2 == 1)
    step(1)
  }
}

class PassThroughRunner extends ChiselFlatSpec with Matchers {
  behavior.of("peek poke tester")

  it should "pass the test" in {
    Driver.execute(
      Array("--generate-vcd-output", "on", "--backend-name", "verilator"),
      () => new PassThroughModule
    ) { dut =>
      new PassThroughTest(dut)
    } should be(true)

  }
}
