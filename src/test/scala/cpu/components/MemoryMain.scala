package cpu.components
import chisel3._
object MemoryMain extends App {
  // run test:runMain cpu.components.MemoryMain in sbt
  iotesters.Driver.execute(args, () => new InstrMem(32,32,1024)) {
    c => new MemoryUnitTester(c)
  }
}
