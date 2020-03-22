package cpu.cache

import chisel3._
import chisel3.util._
import cpu.parameters.CacheParameters

/**
  * the Instruction cache controller, responsible for communicating between cpu and memory and cache
  * I'll make this name even longer to ensure that everything is explicit
  * like controller.cpuBus.in.addr
  * I'm explicitly seperating the valid and signal name to make it more verbose
  */
// input is a sink, so use flipped
class ICacheControllerToCPUIn extends Bundle with CacheParameters {
  val addrBundle = Flipped(Decoupled(UInt(addrWidth.W)))
}

// is a source, so don't use flipped
class ICacheControllerToCPUOut extends Bundle with CacheParameters {
  val instrBundle = Valid(UInt(instWidth.W))
}

class ICacheControllerToCPUIO extends Bundle with CacheParameters {
  val fromCPU = new ICacheControllerToCPUIn
  val toCPU = new ICacheControllerToCPUOut
}

/**
  * from ram to instruction cache
  * NOTICE: the input is a sink, use flipped
  */
class ICacheControllerToRamIn extends Bundle with CacheParameters {
  val blockBundle = Flipped(Valid(UInt(blockSize.W)))
}

// output is a source, so don't flip
class ICacheControllerToRamOut extends Bundle with CacheParameters {
  val addrBundle = Decoupled(UInt(addrWidth.W))
}

class ICacheControllerToRamIO extends Bundle with CacheParameters {
  val toRam = new ICacheControllerToRamOut
  val fromRam = new ICacheControllerToRamIn
}

class ICacheControllerToCacheIn extends Bundle with CacheParameters {
  val instrBundle = Flipped(Valid(UInt(instWidth.W)))
}

class ICacheControllerToCacheOut extends Bundle with CacheParameters {
  // address misalignment should be caught inside CPU
  val addrBundle = Decoupled(UInt(addrWidth.W))
}

class ICacheControllerToCacheIO extends Bundle with CacheParameters {
  val fromCache = new ICacheControllerToCacheIn
  val toCache = new ICacheControllerToCacheOut
}
