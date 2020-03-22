package cpu.cache

import chisel3._
import cpu.parameters.CacheParameters

// fully associative
class ICacheMetaBundle extends Bundle with CacheParameters {
  val validBit = Bool()
  val tag = UInt(tagWidth.W)
  val index = UInt(indexWidth.W)
}

/**
  * I-Cache controller is an interface connecting physical cache and cpu
  * don't connect to ram to build a hierarchical approach
  * mem translate to lutram
  * I have no idea what syncreadmem translates to yet
  * TODO: whether this complete in a single cycle
  */
class ICacheController extends Module with CacheParameters {
  val io = IO(new Bundle() {
    val ramBus = new ICacheControllerToRamIO
    val cpuBus = new ICacheControllerToCPUIO
  })

  /**
    * the valid signals, will translate to lut ram
    * I'm gonna use mem for now, cause vec looks really dirty
    * TODO: which is more efficient: vec or mem
    */
  // there is no dirty bit in the instruction cache
  val metaMem = Mem(numOfBlocks, new ICacheMetaBundle)
  val instrMem = Mem(numOfBlocks, UInt((blockSize * 8).W))
  //TODO: memory initialize

  /**
    * communication with CPU
    * to simplify logic, it should always be ready to receive, but the returning data
    * is not always valid ( cache miss, etc )
    */
  io.cpuBus.fromCPU.addrBundle.ready := true.B

  // always ready to receive, but when instruction not ready, stall the CPU

  //Note: Don't use valid signal to drive logic, but to gate off state updates

  /**
    *
    * @param address the address of the instruction to lookup
    */
  def lookUP(address: UInt): Unit = {
    val addrOffset = address(offsetWidth - 1, 0)
    val addrIndex = address(offsetWidth + indexWidth - 1, offsetWidth)
    val addrTag = address(addrWidth - 1, addrWidth - 1 - tagWidth)
    io.cpuBus.toCPU.instrBundle.valid := false.B
    io.cpuBus.toCPU.instrBundle.bits := DontCare
    for (i: Int <- 0 until numOfBlocks) {
      val metaData = metaMem(i)
      when(metaData.tag === addrTag && metaData.index === addrIndex && metaData.validBit) {
        io.cpuBus.toCPU.instrBundle.valid := true.B
        //TODO: bug here?
        io.cpuBus.toCPU.instrBundle.bits := (instrMem(i))(addrOffset << 5 + 31, addrOffset << 5)
      }
    }
  }

}

