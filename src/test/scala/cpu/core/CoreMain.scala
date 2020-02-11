package cpu.core

import chisel3._
import cpu.common._

object CoreMain extends App {
  implicit val conf = PhoenixConfiguration()
  iotesters.Driver.execute(args,() => new Core) {
    c => new CoreUnitTester(c)
  }
}
