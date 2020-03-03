// See README.md for license details.

package cpu.core.memory

import chisel3._
import chisel3.util._
import cpu.core.Constants._
import cpu.core.bundles.CPBundle

class CP0HandleBundle extends Bundle {
  val status = UInt(dataLen.W)
  val cause = UInt(dataLen.W)
  val EPC = UInt(dataLen.W)
}

class Forward extends Module {
  val io = IO(new Bundle {
    val inCP0 = Input(new CP0HandleBundle)
    val wbCP0 = Input(new CPBundle)
    val outCP0 = Output(new CP0HandleBundle)
  })

  io.outCP0 <> io.inCP0
  when(io.wbCP0.enable){
    switch(io.wbCP0.address){
      is(con_Status.addr.U){
        io.outCP0.status := io.wbCP0.data
      }
      is(con_Cause.addr.U){
        io.outCP0.cause := io.wbCP0.data
      }
      is(con_EPC.addr.U){
        io.outCP0.EPC := io.wbCP0.data
      }
    }
  }


}
