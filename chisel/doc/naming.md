## naming the whole module
this will effectively preserve all the names that are in this module
```scala
@chiselName
class MyModule extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val in2 = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })
  val sum = io.in + io.in2 // this is a top-level val, will be named

  // A method, we can call to help generate code:
  def inc(x: UInt): UInt = {
    val incremented = x + 1.U // We cannot name this, it's inside a method
    incremented
  }

  io.out := inc(sum)
}
```
## naming a specific signal
calling `.suggestName` on a `val` will preserve the name of it
 ```scala
def inc(x: UInt): UInt = {
    val incremented = x + 1.U // We cannot name this, it's inside a method
    incremented.suggestName("incremented") // Now it is named!
  }
``` 
## change the name of the module
```scala
class Coffee extends BlackBox {
    val io = IO(new Bundle {
        val I = Input(UInt(32.W))
        val O = Output(UInt(32.W))
    })
    override def desiredName = "Tea"
}
class Salt extends Module {
    val io = IO(new Bundle {})
    val drink = Module(new Coffee)
    override def desiredName = "SodiumMonochloride"
}
```
This will produce the verilog
**note how the names are Tea and Sodum...**
```verilog
module SodiumMonochloride(
  input   clock,
  input   reset
);
  wire [31:0] drink_O;
  wire [31:0] drink_I;
  Tea drink (
    .O(drink_O),
    .I(drink_I)
  );
  assign drink_I = 32'h0;
endmodule
```