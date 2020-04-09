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
https://github.com/alexforencich/verilog-axi/blob/master/rtl/arbiter.v
by Discreater
*/
package axi

import chisel3._
import chisel3.util._

/**
  *
  * @param PORTS        port number
  * @param TYPE         "PRIORITY"
  *                     "ROUND_ROBIN"
  * @param BLOCK        "NONE"
  *                     "REQUEST"
  *                     "ACKNOWLEDGE"
  * @param LSB_PRIORITY "LOW"
  *                     "HIGH"
  */
class Arbiter(PORTS: Int = 4, TYPE: String = "PRIORITY", BLOCK: String = "NONE", LSB_PRIORITY: String = "LOW") extends Module {
  assert(PORTS > 0)
  assert(Seq("PRIORITY", "ROUND_ROBIN").contains(TYPE))
  assert(Seq("NONE", "REQUEST", "ACKNOWLEDGE").contains(BLOCK))
  assert(Seq("LOW", "HIGH").contains(LSB_PRIORITY))
  val io = IO(new Bundle {
    val request = Input(UInt(PORTS.W))
    val acknowledge = Input(UInt(PORTS.W))

    val grant = Output(new EncoderBundle(WIDTH = PORTS))
  })
  val grant_reg = RegInit(0.U.asTypeOf(new EncoderBundle(WIDTH = PORTS)))
  val mask_reg = RegInit(0.U(PORTS.W))

  val inst = Module(new PriorityEncoder(PORTS, LSB_PRIORITY))
  val masked = Module(new PriorityEncoder(PORTS, LSB_PRIORITY))
  inst.io.in.bits := io.request
  masked.io.in.bits := io.request & mask_reg

  def rrHandle(encoder: PriorityEncoder) = {
    grant_reg <> encoder.io.out
    mask_reg :=
      (if (LSB_PRIORITY == "LOW") Fill(PORTS, 1.U(1.W)) >> (PORTS.U - encoder.io.out.encoded)
      else Fill(PORTS, 1.U(1.W)) << (encoder.io.out.encoded + 1.U))
  }

  def validHandle() = {
    if (TYPE == "PRIORITY") { // priority
      grant_reg <> inst.io.out
    } else { // round robin
      when(masked.io.out.valid) {
        rrHandle(masked)
      }.otherwise {
        rrHandle(inst)
      }
    }
  }

  grant_reg := 0.U.asTypeOf(new EncoderBundle(WIDTH = PORTS)) // if no match, set to 0
  if (BLOCK == "REQUEST") {
    when((grant_reg.bits & io.request) =/= 0.U) {
      // granted request still asserted; hold it
      grant_reg := grant_reg
    }.elsewhen(inst.io.out.valid) {
      validHandle()
    }
  } else if(BLOCK == "ACKNOWLEDGE") {
    when(grant_reg.valid && ((grant_reg.bits & io.acknowledge) === 0.U)) {
      // granted request not yet acknowledged; hold it
      grant_reg := grant_reg
    }.elsewhen(inst.io.out.valid) {
      validHandle()
    }
  }

  io.grant <> grant_reg
}
