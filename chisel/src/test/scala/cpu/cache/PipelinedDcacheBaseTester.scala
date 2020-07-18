package cpu.cache

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util.Decoupled
import cpu.CPUConfig
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.dataCache.DataCache
import verification.VeriAXIRam

import scala.collection.mutable

case class request(address: Int, writeEnable: Boolean, writeData: List[Int], writeMask: List[Boolean]) {}

class PipelinedDcacheBaseTester(dut: VeriDCache, goldenMem: PerfectMemory) extends PeekPokeTester(dut) {
  val cycleLimit = 1000
  // a request travels from response queue
  val requestQueue   = new scala.collection.mutable.Queue[request]
  val responseQueue  = new scala.collection.mutable.Queue[request]
  val readValidQueue = new mutable.Queue[Boolean]
  var cycleCount: Int = 0
  def init(): Unit = {
    reset(3)
    cycleCount = 0
  }

  def memRequest(req: request): Unit = {
    requestQueue.enqueue(req)
  }

  def next(): Boolean = {
    if (requestQueue.nonEmpty) {
      val newReq = requestQueue.head
      poke(dut.io.request.valid, true)
      poke(dut.io.request.bits.address, newReq.address)
      poke(
        dut.io.request.bits.writeMask,
        newReq.writeMask.zipWithIndex.map { case (value, index) => value << (8 * index) }.sum
      )
      poke(
        dut.io.request.bits.writeData,
        newReq.writeData.zipWithIndex.map { case (value, index) => value << index }.sum
      )

      if (peek(dut.io.request.ready) == 1) {
        responseQueue.enqueue(newReq)
        if (newReq.writeMask.zipWithIndex.map { case (value, index) => value << (8 * index) }.sum != 0) {
          goldenMem.writeToMem(newReq.address, newReq.writeData, newReq.writeMask)
        }
        requestQueue.dequeue()
      }
    } else {
      poke(dut.io.request.valid, false)
      poke(dut.io.request.bits.address, 0)
      poke(dut.io.request.bits.writeMask, 0)
      poke(dut.io.request.bits.writeData, 0)
    }
    readValidQueue.enqueue(peek(dut.io.dataValid) == 1)
    if (readValidQueue.nonEmpty) {
      // if this data is valid
      if (readValidQueue.head) {
        val rsp = responseQueue.head
        if (!rsp.writeEnable) {
          expect(
            dut.io.dataOutput,
            goldenMem.readFromMem(rsp.address).zipWithIndex.map { case (value, index) => value << (8 * index) }.sum,
            s"the read request is ${rsp.address} the data output is ${peek(dut.io.dataOutput)
              .toString(16)}, the expected result is ${goldenMem
              .readFromMem(rsp.address)
              .zipWithIndex
              .map { case (value, index) => value << (8 * index) }
              .sum
              .toHexString(16)}"
          )
        }
        responseQueue.dequeue
      }
      readValidQueue.dequeue()
    }
    cycleCount = cycleCount + 1
    step(1)
    (requestQueue.nonEmpty || responseQueue.nonEmpty) && (cycleCount < cycleLimit)
  }

}

class VeriDCache extends Module {
  val io = IO(new Bundle {
    val request = Flipped(Decoupled(new Bundle {
      val address   = UInt(32.W)
      val writeMask = UInt(4.W)
      val writeData = UInt(32.W)
    }))
    val dataOutput = Output(UInt(32.W))
    val dataValid  = Output(Bool())
  })
  implicit val cacheConfig: CacheConfig = new CacheConfig
  implicit val cpuConfig:   CPUConfig   = new CPUConfig(build = false)
  val dcache  = Module(new DataCache)
  val veriRam = Module(new VeriAXIRam)
  dcache.io.axi     <> veriRam.io.axi
  dcache.io.request <> io.request
  io.dataValid      := dcache.io.commit
  io.dataOutput     := dcache.io.readData
}
