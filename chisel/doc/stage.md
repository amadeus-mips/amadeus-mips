# How to use ChiselStage

## generate the verilog
```scala
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import firrtl.options.TargetDirAnnotation

class Foo extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  io.out := ~io.in
}

object elaborateFoo extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() => new Foo()),
      TargetDirAnnotation("verilog"))
  )
}

object elaborateToVerilogString extends App {
  println((new ChiselStage).emitVerilog(new Foo()))
}
```
 there are 2 ways to generate verilog from chisel modules
 - use `emitVerilog` on an instance of the module. This will return a string that can be written to file
 - use `ChiselGeneratorAnnotation(() => new foo())`, this will generate a verilog **file**
 
 ## other things
 most of the options can be displayed by adding `"--help"` to the argument array
