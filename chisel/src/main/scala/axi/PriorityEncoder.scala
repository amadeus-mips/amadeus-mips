/*

Copyright (c) 2014-2018 Alex Forencich

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Modified from
https://github.com/alexforencich/verilog-axi/blob/master/rtl/priority_encoder.v
by Discreater
*/
package axi

import chisel3._
import chisel3.util._

class PriorityEncoderIO(WIDTH: Int) extends Bundle {
  val in = Input(new Bundle() {
    val bits = UInt(WIDTH.W)
  })
  val out = Output(new EncoderBundle(WIDTH))

  override def cloneType: PriorityEncoderIO.this.type = new PriorityEncoderIO(WIDTH).asInstanceOf[this.type ]
}

/**
  *
  * @param WIDTH        width of input
  * @param LSB_PRIORITY "LOW"
  *                     "HIGH"
  */
class PriorityEncoder(WIDTH: Int = 4, LSB_PRIORITY: String = "LOW") extends Module {
  assert(WIDTH >= 1)
  assert(LSB_PRIORITY == "LOW" || LSB_PRIORITY == "HIGH")
  val io = IO(new PriorityEncoderIO(WIDTH))

  if(WIDTH == 1){
    // one input
    io.out.valid := io.in.bits
    io.out.encoded := 0.U
  } else if(WIDTH == 2) {
    // two inputs - just an OR gate
    io.out.valid := io.in.bits.orR()
    io.out.encoded := (if(LSB_PRIORITY == "LOW") io.in.bits(1) else !io.in.bits(0))
  } else {
    // more than two inputs - split into two parts and recurse
    // also pad input to correct power-of-two width

    // power-of-two width
    val W1 = 1 << log2Ceil(WIDTH)
    val W2 = W1 / 2

    val inst1 = Module(new PriorityEncoder(W2, LSB_PRIORITY))
    inst1.io.in.bits := io.in.bits(W2-1, 0)
    val valid1 = inst1.io.out.valid
    val out1 = inst1.io.out.encoded

    val inst2 = Module(new PriorityEncoder(W2, LSB_PRIORITY))
    inst2.io.in.bits := io.in.bits(WIDTH-1, W2)
    val valid2 = inst2.io.out.valid
    val out2 = inst2.io.out.encoded

    io.out.valid := valid1 || valid2
    if(LSB_PRIORITY == "LOW") {
      io.out.encoded := Mux(valid2, Cat(1.U(1.W), out2), Cat(0.U(1.W), out1))
    } else {
      io.out.encoded := Mux(valid1, Cat(0.U(1.W), out1), Cat(1.U(1.W), out2))
    }
  }

  io.out.bits := 1.U << io.out.encoded

}


