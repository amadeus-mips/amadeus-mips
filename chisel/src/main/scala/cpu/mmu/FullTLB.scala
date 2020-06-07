package cpu.mmu

import chisel3._
import chisel3.util._

/**
  * this is a TLB that only translate the upper bits
  *
  * @param numOfReadPorts how many read ports there are
  * @param TLBSize        how many entries are in TLB
  */
class FullTLB(numOfReadPorts: Int, TLBSize: Int) extends Module {
  require(isPow2(TLBSize), "TLB size should be a power of 2")
  val io = IO(new Bundle {
    val asid = Input(UInt(8.W))
    val kseg0Uncached = Input(Bool())
    val query = Input(Vec(numOfReadPorts, new TLBQuery()))
    val result = Output(Vec(numOfReadPorts, new TLBResult()))

    // there should be only 1 operation port that can handle both read and write
    val instrReq = Input(new TLBRWReq(TLBSize))
    val readResp = Output(new TLBEntry)

    // the probing instruction
    val probeReq = Input(UInt(19.W))
    val probeResp = Output(UInt(32.W))
  })

  val physicalTLB = RegInit(VecInit(Seq.fill(TLBSize)(0.U.asTypeOf(new TLBEntry))))

  // read port for I-cache, (D-cache, uncached)
  // NOTE: query is executed regardless of whether the input is valid, validity of input
  // will only be judged by outer circuit
  val hitWire = Wire(Vec(numOfReadPorts, Vec(TLBSize, Bool())))
  val hitIndex = Wire(Vec(numOfReadPorts, UInt(log2Ceil(TLBSize).W)))
  for (i <- 0 until numOfReadPorts) {
    hitWire(i) := physicalTLB.map((entry: TLBEntry) =>
      (entry.vpn2 === io.query(i).vAddr(19, 1)) && ((entry.asid === io.asid) || entry.global)
    )
    val isHit = hitWire(i).contains(true.B)
    val indexTLB = hitWire(i).indexWhere((hit: Bool) => hit)
    val page = physicalTLB(hitIndex(i)).pages(io.query(i).vAddr(0))
    val mapped =
      (!io.query(i).vAddr(19)) || (io.query(i).vAddr(19, 18) === "b11".U(2.W))
    val uncached = (io.query(i).vAddr(19, 17) === "b101".U(3.W)) || (io.query(i).vAddr(19, 17) === "b100".U(3.W) && io.kseg0Uncached)
    hitIndex(i) := indexTLB
    io.result(i).hit := isHit
    io.result(i).pageInfo := page
    when(!mapped) {
      io.result(i).pageInfo.pfn := Cat(0.U(3.W), io.query(i).vAddr(16,0))
    }
    io.result(i).mapped := mapped
    io.result(i).uncached := uncached
  }

  // the probe request and response
  val probeWire = Wire(Vec(TLBSize, Bool()))
  probeWire := physicalTLB.map((entry: TLBEntry) => entry.vpn2 === io.probeReq && (entry.global || entry.asid === io.instrReq.writeData.asid))
  io.probeResp := Cat(
    !probeWire.contains(true.B),
    0.U((32 - log2Ceil(TLBSize) - 1).W),
    probeWire.indexWhere((hit: Bool) => hit)
  )

  // the read and write request
  when(io.instrReq.writeEn) {
    physicalTLB(io.instrReq.TLBIndex) := io.instrReq.writeData
  }
  io.readResp := physicalTLB(io.instrReq.TLBIndex)
}
