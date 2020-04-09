/*------------------------------------------------------------------------------
--------------------------------------------------------------------------------
Copyright (c) 2016, Loongson Technology Corporation Limited.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of Loongson Technology Corporation Limited nor the names of
its contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL LOONGSON TECHNOLOGY CORPORATION LIMITED BE LIABLE
TO ANY PARTY FOR DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--------------------------------------------------------------------------------
------------------------------------------------------------------------------*/

//*************************************************************************
//   > File Name   : soc_top.v
//   > Description : SoC, included cpu, 2 x 3 bridge,
//                   inst ram, confreg, data ram
//
//           -------------------------
//           |           cpu         |
//           -------------------------
//                       | axi
//                       |
//             ---------------------
//             |    1 x 2 bridge   |
//             ---------------------
//                  |            |
//                  |            |
//             -----------   -----------
//             | axi ram |   | confreg |
//             -----------   -----------
//
//   > Author      : LOONGSON
//   > Date        : 2017-08-04
//*************************************************************************
package soc

/** Modified by Discreater */
import axi.{AXIInterconnect, AXIInterconnectConfig}
import chisel3._
import common.{DebugBundle, GPIO}
import confreg.Confreg
import cpu.CPUTop
import memory.memoryAXIWrap

class SocLiteTop(simulation: Boolean = false, memFile: String) extends Module {
  val io = IO(new Bundle {
    val gp = new GPIO
    val debug = Output(new DebugBundle)
  })

  val cpu = Module(new CPUTop)

  /** 1x2 interconnect */
  val axiInterconnect = Module(new AXIInterconnect(AXIInterconnectConfig.loongson_func))
  val confreg = Module(new Confreg(simulation))
  val ram = Module(new memoryAXIWrap(memFile))

  axiInterconnect.io.slaves(0) <> cpu.io.bus_axi
  axiInterconnect.io.masters(0) <> ram.io.axi
  axiInterconnect.io.masters(1) <> confreg.io.axi

  cpu.io.intr := 0.U // high active
  io.gp <> confreg.io.gp
  io.debug <> cpu.io.debug
}
