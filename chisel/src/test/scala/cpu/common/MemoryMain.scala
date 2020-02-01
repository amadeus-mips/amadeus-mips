package cpu.common
import chisel3._
object MemoryMain extends App {
  iotesters.Driver.execute(args, () => new InstrMem(32,32,1024)) {
    c => new MemoryUnitTester(c)
  }
}
