package cpu.core {
  import chisel3._
  import cpu.common.{DataMem, InstrMem, PhoenixConfiguration}

  class CoreIO(implicit val config: PhoenixConfiguration) extends Bundle{
    val pc_out = Output(UInt(config.regLen.W))
  }

  class Core(implicit val config: PhoenixConfiguration) extends Module {
    val io = IO(new Bundle
     {
       val out = new CoreIO()
     })
    val control = Module(new ControlPath())
    val data = Module(new DataPath())

    control.io.control <> data.io.control
    control.io.data <> data.io.data
    io.out.pc_out := data.io.data.instruction
  }

  object CoreDriver extends App {
    //TODO: how to test with implicit
    implicit val conf = PhoenixConfiguration()
    // this line would turn off all optimizations ( not recommended )
    //chisel3.Driver.execute(Array("--target-dir", "generated","-X","mverilog"), () => new Core())
    chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Core())
  }
}
