package cpu.singleCycle

import chisel3._
import cpu.components._
object DecodeMain extends App {
  //run test:runMain cpu.singleCycle.DecodeMain in sbt
  //TODO: testing n implicit
  implicit val conf = PhoenixConfiguration()
  iotesters.Driver.execute(args, () => new Decode()){
    c => new DecodeUnitTester(c)
  }
}
