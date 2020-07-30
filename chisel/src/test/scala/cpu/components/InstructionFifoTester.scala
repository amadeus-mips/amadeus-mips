package cpu.components
import chisel3.iotesters.PeekPokeTester
import cpu.core.components.InstructionFIFO

class InstructionFifoTester(dut: InstructionFIFO[chisel3.UInt]) extends PeekPokeTester(dut) {
  val readyFifo  = scala.collection.mutable.Queue[Int]()
  val outputFifo = scala.collection.mutable.Queue[Int]()

  def push(seq: Seq[Int]): Unit = {
    for (i <- seq) {
      readyFifo.enqueue(i)
    }
  }

  def next(): Unit = {
    while (readyFifo.nonEmpty || outputFifo.nonEmpty) {
      poke(dut.io.enqueue(0).valid, readyFifo.nonEmpty)
      poke(dut.io.enqueue(1).valid, false)
      poke(dut.io.enqueue(0).bits, if (readyFifo.nonEmpty) readyFifo.head else 0)
      poke(dut.io.enqueue(1).bits, 0)
      if (peek(dut.io.readyForEnqueue) == 1 && readyFifo.nonEmpty) {
        outputFifo.enqueue(readyFifo.dequeue())
      }
      poke(dut.io.dequeue(0).ready, true)
      poke(dut.io.dequeue(1).ready, true)
      if (peek(dut.io.dequeue(0).valid) == 1) {
        expect(dut.io.dequeue(0).bits, outputFifo.dequeue())
      }
      if (peek(dut.io.dequeue(1).valid) == 1) {
        expect(dut.io.dequeue(1).bits, outputFifo.dequeue())
      }
      step(1)
    }
  }
}
