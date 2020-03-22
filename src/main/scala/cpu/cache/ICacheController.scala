package cpu.cache

import chisel3._
import chisel3.util._
import cpu.parameters.CacheParameters

// fully associative
class ICacheMetaBundle extends Bundle with CacheParameters {
  val validBit = Bool()
  val tag = UInt(tagWidth.W)
  val index = UInt(indexWidth.W)
}

object ICacheStates {
  // shouldn't be idle state, as there must be read every cycle
  val cache_read :: cache_refill :: Nil = Enum(4)
}

/**
  * underlying I-cache is direct-mapped for simplicity
  * TODO: handle multi-way cache generation
  * I-Cache controller is an interface connecting physical cache and cpu
  * don't connect to ram to build a hierarchical approach
  * mem translate to lutram
  * I have no idea what syncreadmem translates to yet
  * this is a direct mapped cache
  * TODO: whether this complete in a single cycle
  */
import cpu.cache.ICacheStates._
class ICacheController extends Module with CacheParameters {
  val io = IO(new Bundle() {
    val ramBus = new ICacheControllerToRamIO
    val cpuBus = new ICacheControllerToCPUIO
  })

  // the status register for the FSM
  val stateReg = RegInit(cache_read)

  // initial values for IO, prevent accidents and compile errors
  io.ramBus.toRam.addrBundle.valid := false.B
  io.ramBus.toRam.addrBundle.bits := DontCare
  io.cpuBus.toCPU.instrBundle.valid := false.B
  io.cpuBus.toCPU.instrBundle.bits := DontCare
  // if the cache is waiting for a refill, then return not ready
  io.cpuBus.fromCPU.addrBundle.ready := (stateReg =/= cache_refill)

  // there is no dirty bit in the instruction cache
  // the valid bit vector
  val validBitVec = RegInit(VecInit(Seq.fill(numOfBlocks)(false.B)))
  val tagCache = Mem(numOfBlocks, UInt(tagWidth.W))
  // change to vec of vec of vec
  val instrCache = Mem(numOfBlocks, UInt((blockSize*8).W))

  // wire the read address from CPU
  val readAddr = Wire(UInt(32.W))
  readAddr := io.cpuBus.fromCPU.addrBundle.bits

  val index = readAddr(offsetWidth + indexWidth - 1,offsetWidth)
  val tag = readAddr(31, 32 - tagWidth)

  // see if the read is a hit
  // check if there is a hit
  val readHit = Wire(Bool())
  readHit := validBitVec(index) & (tagCache(index) === tag)


  switch(stateReg) {
    is (cache_read) {
      when (!readHit) {
        stateReg := cache_refill

      }.otherwise {
        // when there is a hit, return the data
        io.cpuBus.toCPU.instrBundle.valid := true.B
        val cacheLine = instrCache(index)
        val actualOffset = readAddr(offsetWidth - 1, 0)
        // original: byte addressed, shift 2: word addressed
        val instrOffsetWidth = log2Ceil(instWidth >> 3)
        val instrOffset = actualOffset >> instrOffsetWidth
        // array.range equals a until b
        //TODO: a complicated trick, should I use vec of vec?
        //TODO: refactor to vec of vec
        //TODO: don't optimize on the fly
        io.cpuBus.toCPU.instrBundle.bits := MuxLookup(instrOffset, DontCare, Array.range(0 ,2^(offsetWidth- instrOffsetWidth)) map (i => (i.U -> cacheLine(i << 5 + instWidth - 1, i << 5).asUInt))
      }
    }
    is (cache_refill) {
      //TODO: don't use valid to drive logic
      when (io.ramBus.fromRam.blockBundle.valid) {
        stateReg := cache_read
      }
    }
  }
}

