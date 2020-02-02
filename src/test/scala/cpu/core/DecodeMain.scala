package cpu.core

import chisel3._
import cpu.common._
object DecodeMain extends App {
  //run test:runMain cpu.core.DecodeMain in sbt
  //TODO: testing n implicit
  implicit val conf = PhoenixConfiguration()
  iotesters.Driver.execute(args, () => new Decode()){
    c => new DecodeUnitTester(c)
  }
}
