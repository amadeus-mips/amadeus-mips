package cpu.core.components

import chisel3._
import chisel3.experimental.{requireIsChiselType, DataMirror}
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import firrtl.options.TargetDirAnnotation

//TODO: bypassing
class InstructionFIFO[T <: Data](gen: T)(implicit CPUConfig: CPUConfig) extends Module {

  // this comes from chisel3.Queue
  val genType = if (compileOptions.declaredTypeMustBeUnbound) {
    requireIsChiselType(gen)
    gen
  } else {
    if (DataMirror.internal.isSynthesizable(gen)) {
      chiselTypeOf(gen)
    } else {
      gen
    }
  }

  val io = IO(new Bundle {

    /** this will flush everything but the entry in head pointer */
    val flushTail = Input(Bool())

    /** enqueue data structure */
    val enqueue         = Vec(CPUConfig.fetchAmount, Flipped(Valid(genType)))
    val readyForEnqueue = Output(Bool())

    /** dequeue data structure */
    val dequeue = Vec(CPUConfig.decodeWidth, Decoupled(genType))
  })

  val capacity: Int = CPUConfig.instructionFIFOLength
  require(capacity > CPUConfig.fetchAmount)
  require(capacity % CPUConfig.fetchAmount == 0)
  require(capacity > CPUConfig.decodeWidth)
  require(capacity % CPUConfig.decodeWidth == 0)
  val size    = RegInit(0.U((log2Ceil(capacity) + 1).W))
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))

  val dataArray  = Reg(Vec(capacity, genType))
  val validArray = RegInit(VecInit(Seq.fill(capacity)(false.B)))

  /** how many valid enqueue requests are there?
    * This has not been handshaked */
  val enqueueRequestValidArray = io.enqueue.map(entry => entry.valid)

  /** calculate how many entries are required to accommodate this enqueue request */
  val numOfEnqueueRequests = Wire(UInt(log2Ceil(capacity).W))
  numOfEnqueueRequests := enqueueRequestValidArray.map(_.asUInt).foldLeft(0.U((log2Ceil(capacity) + 1).W))(_ + _)

  /** there are capacity - size entries left for enqueue */
  val spaceLeft = WireDefault(capacity.U - size)

  /** if this cycle, the enqueue request is ready */
  val enqueueReady = WireDefault(spaceLeft >= numOfEnqueueRequests)

  /** how many valid dequeue requests are there */
  val dequeueResponseValidArray = Wire(Vec(CPUConfig.decodeWidth, Bool()))

  /** calculate how many elements are de-queued in the handshake
    * this value *has been handshaked* */
  val numOfDequeueElements = Wire(UInt((log2Ceil(capacity) + 1).W))
  numOfDequeueElements := dequeueResponseValidArray
    .zip((0 until CPUConfig.decodeWidth).map { i: Int => io.dequeue(i).ready })
    .map { case (ready: Bool, valid: Bool) => ready && valid }
    .foldLeft(0.U((log2Ceil(capacity) + 1).W))((a: UInt, b: Bool) => a + b.asUInt)

  // IO section
  io.readyForEnqueue := enqueueReady

  for (i <- 0 until CPUConfig.decodeWidth) {
    io.dequeue(i).valid := dequeueResponseValidArray(i)
    io.dequeue(i).bits  := dataArray(tailPTR - i.U)
  }

  // assert(size >= numOfDequeueElements)
  // cover size = 0, size = number of dequeue elements ( this must succeed )
  // assert io.dequeue.ready: 1111..000

  for (i <- 0 until CPUConfig.fetchAmount) {
    // write signal
    when(enqueueRequestValidArray(i) && enqueueReady) {
      dataArray(headPTR - i.U) := io.enqueue(i).bits
      assert(!validArray(headPTR - i.U))
      assert(io.enqueue(i).valid)
      validArray(headPTR - i.U) := true.B
    }
  }

  for (i <- 0 until CPUConfig.decodeWidth) {
    dequeueResponseValidArray(i) := validArray(tailPTR - i.U)
    when(io.dequeue(i).ready && dequeueResponseValidArray(i)) {
      validArray(tailPTR - i.U) := false.B
    }
  }

  when(enqueueReady) {
    headPTR := headPTR - numOfEnqueueRequests
  }

  tailPTR := tailPTR - numOfDequeueElements

  size := Mux(
    enqueueReady,
    size - numOfDequeueElements + numOfEnqueueRequests,
    size - numOfDequeueElements
  )

  when(io.flushTail) {
    headPTR             := tailPTR - 1.U
    size                := 1.U
    validArray          := 0.U.asTypeOf(validArray)
    validArray(tailPTR) := true.B && !io.dequeue.head.ready
  }

  assert(!io.flushTail || (io.flushTail && size =/= 0.U))
}

object InstructionFIFOElaborate extends App {
  implicit val CPUConfig = new CPUConfig(build = false)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new InstructionFIFO(UInt(32.W))), TargetDirAnnotation("verification"))
  )
}
