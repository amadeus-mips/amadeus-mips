package cpu.core.components

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cpu.CPUConfig
import firrtl.options.TargetDirAnnotation

class InstructionFIFO[T <: Data](gen: T)(implicit CPUConfig: CPUConfig) extends Module {
  val io = IO(new Bundle {

    /** enqueue data structure */
    val enqueue         = Vec(CPUConfig.fetchAmount, Flipped(Valid(gen)))
    val readyForEnqueue = Output(Bool())

    /** dequeue data structure */
    val dequeue         = Vec(CPUConfig.decodeWidth, Valid(gen))
    val readyForDequeue = Input(Bool())
  })
  val capacity: Int = CPUConfig.instructionFIFOLength
  require(capacity > CPUConfig.fetchAmount)
  require(capacity % CPUConfig.fetchAmount == 0)
  require(capacity > CPUConfig.decodeWidth)
  require(capacity % CPUConfig.decodeWidth == 0)
  val size    = RegInit(0.U((log2Ceil(capacity) + 1).W))
  val headPTR = RegInit(0.U(log2Ceil(capacity).W))
  val tailPTR = RegInit(0.U(log2Ceil(capacity).W))

  val dataArray  = Mem(capacity, gen)
  val validArray = RegInit(VecInit(Seq.fill(capacity)(false.B)))

  val enqueueRequestValidArray = Wire(Vec(CPUConfig.decodeWidth, Bool()))
  enqueueRequestValidArray := io.enqueue.map(entry => entry.valid)

  val dequeueResponseValidArray = Wire(Vec(CPUConfig.decodeWidth, Bool()))

  /** calculate how many entries are required to accommodate this enqueue request */
  val numOfEnqueueRequests =
    enqueueRequestValidArray.asTypeOf(Vec(CPUConfig.decodeWidth, UInt(1.W))).reduce(_ + _)

  /** calculate how many elements can be dequeue-ed */
  val numOfDequeueValidElements =
    dequeueResponseValidArray.asTypeOf(Vec(CPUConfig.decodeWidth, UInt(1.W))).reduce(_ + _)

  /** there are capacity - size entries left */
  val spaceLeft = capacity.U - size

  /** if this cycle, the enqueue request is ready */
  val enqueueReady = WireDefault(spaceLeft >= numOfEnqueueRequests)

  io.readyForEnqueue := enqueueReady

  when(enqueueReady) {
    tailPTR := tailPTR - numOfEnqueueRequests
  }

  when(io.readyForDequeue) {
    headPTR := headPTR - numOfDequeueValidElements
  }

  size := MuxCase(
    size,
    Array(
      (io.readyForDequeue && enqueueReady)  -> (size - numOfDequeueValidElements + numOfEnqueueRequests),
      (io.readyForDequeue && !enqueueReady) -> (size - numOfDequeueValidElements),
      (!io.readyForDequeue && enqueueReady) -> (size + numOfEnqueueRequests)
    )
  )

  for (i <- 0 until CPUConfig.decodeWidth) {
    // write signal
    when(enqueueRequestValidArray(i) && enqueueReady) {
      dataArray(tailPTR + i.U) := io.enqueue(i).bits
      assert(!validArray(tailPTR + i.U))
      assert(io.enqueue(i).valid)
      validArray(tailPTR + i.U) := io.enqueue(i).valid
    }
    // write signal
    val dataValue = Wire(gen)
    dataValue                    := validArray(headPTR - i.U)
    io.dequeue(i).bits           := dataArray(headPTR - i.U)
    io.dequeue(i).valid          := dataValue
    dequeueResponseValidArray(i) := validArray(headPTR - i.U)
  }
}

object InstructionFIFOElaborate extends App {
  implicit val CPUConfig = new CPUConfig(build = false)
  (new ChiselStage).execute(
    Array(),
    Seq(ChiselGeneratorAnnotation(() => new InstructionFIFO(UInt(32.W))), TargetDirAnnotation("verification"))
  )
}
