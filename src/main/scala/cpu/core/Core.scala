package cpu.core {
  import chisel3._
  import cpu.common.{DataMem, InstrMem, PhoenixConfiguration}

  class Core(implicit val config: PhoenixConfiguration) extends Module {
    val io = IO(new Bundle
     {
       val nonsense = Output(Bool())
     })
    io.nonsense := DontCare
    val control = Module(new ControlPath())
    val data = Module(new DataPath())

    control.io.control <> data.io.control
    control.io.data <> data.io.data

  }

  object CoreDriver extends App {
    //TODO: how to test with implicit
    implicit val conf = PhoenixConfiguration()
    chisel3.Driver.execute(args, () => new Core())
  }
}
