package cpu.cache

import chisel3._
import chisel3.util._
import chisel3.ExplicitCompileOptions.Strict
import chisel3.internal.naming.chiselName

/**
  * @source https://gist.github.com/kammoh/ceafdc3c0ce50cd236ca41dfe671495c
  * @param size
  * @param elType
  * @param numPorts
  * @tparam T
  */
@chiselName
class MultiPortedRam[T <: Data](size: Int, elType: T, numPorts: Int = 2) extends Module {
  val io = IO(new Bundle {
    val port = Vec(numPorts, new Bundle {
      val req = Flipped(Valid(new Bundle {
        val addr = UInt(log2Ceil(size).W)
        val write = Valid(elType.cloneType)
      }))
      val resp = Valid(elType.cloneType)
    })
  })

  private val ram = SyncReadMem(size, elType).suggestName(name)

  io.port.foreach(port => {
    val mkPort = ram(port.req.bits.addr)
    mkPort.suggestName(name + "Port")
    port.resp.valid := RegNext(port.req.valid, 0.B)
    port.resp.bits := DontCare
    when(port.req.valid) {
      port.resp.bits := mkPort
      when(port.req.bits.write.valid) {
        mkPort := port.req.bits.write.bits
      }
    }
  })
}