package cpu.core {
  import chisel3._
  import cpu.common.{DataMem, InstrMem, PhoenixConfiguration}

  class CoreIO(implicit val config: PhoenixConfiguration) extends Bundle {
    val instrMem = Module(new InstrMem(config.memAddressWidth,config.memDataWidth, config.memSize))
    val dataMem = Module(new DataMem(config.memAddressWidth,config.memDataWidth, config.memSize))
  }

  class Core(implicit val config: PhoenixConfiguration) extends Module {
  val io = IO(new CoreIO())
    io := DontCare

    val control = Module(new ControlPath())
    val data = Module(new DataPath())

    control.io.control <> data.io.control
    control.io.data <> data.io.data

    control.io.instrMem.io <>io.instrMem.io
    io.instrMem.io <> data.io.instrMem.io

    io.dataMem.io <> control.io.dataMem.io
    io.dataMem.io <> data.io.dataMem.io
  }

  object CoreDriver extends App {
    //TODO: how to test with implicit
    implicit val conf = PhoenixConfiguration()
    chisel3.Driver.execute(args, () => new Core())
  }
}
