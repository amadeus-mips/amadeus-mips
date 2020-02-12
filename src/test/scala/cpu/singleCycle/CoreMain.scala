package cpu.singleCycle

import chisel3._
import cpu.components._

object CoreMain extends App {
  implicit val conf = PhoenixConfiguration()
  iotesters.Driver.execute(args,() => new Core) {
    c => new CoreUnitTester(c)
  }
}
