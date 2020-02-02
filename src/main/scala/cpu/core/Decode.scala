package cpu.core {
  import chisel3._
  import chisel3.util._
  import cpu.common._
  import cpu.common.Instructions._
  import cpu.core._

  //TODO: decide on all kinds of control signals later on, we can always add support here
  /**
    * the decode output
    * @param conf: the phoenix configuration
    */
  class DecodeOutIO(implicit val conf: PhoenixConfiguration) extends Bundle{
    // select the ALU
    val rsAddress = UInt(5.W)
    val rtAddress = UInt(5.W)
    val rdAddress = UInt(5.W)
    // TODO: add support for reading data from RegFile
    // select signal to memory
  }

  /**
    * the actual decode stuff
    * TODO: document this: decode only fetches mem, wb etc. Control signal is handled
    * seperately
     * @param conf: the phoenix configuration
    */
  class Decode(implicit val conf: PhoenixConfiguration) extends Module {
    val io = IO(new Bundle() {
      val instru = Input(UInt(conf.regLen.W))
      val decodeOut = Output(new DecodeOutIO())
    })
    // how to construct a wire
    val instrWire = Wire(UInt())
    instrWire := io.instru
    val rsAddress = instrWire(25,21)
    val rtAddress = instrWire(20,16)
    val rdAddress = instrWire(15,11)

    io.decodeOut.rsAddress := rsAddress
    io.decodeOut.rtAddress := rtAddress
    io.decodeOut.rdAddress := rdAddress

    // pattern matching with instructions
//    switch (io.instru) {
//      is(ADD) {
        // great, now we are reassigning to val, seriously?
        // TODO: wait, my bad, didn't use the := drive signal...
        // I'm gonna start some testing now
//        val
//      }
//    }

  }

}
