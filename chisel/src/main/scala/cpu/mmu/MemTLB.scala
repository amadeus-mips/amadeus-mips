package cpu.mmu

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

//noinspection DuplicatedCode
@chiselName
class MemTLB(numOfReadPorts: Int, tlbSize: Int) extends BaseTLB(numOfReadPorts, tlbSize) {
  val physicalTLB = Mem(tlbSize, UInt((new TLBEntry).getWidth.W))
  val valid = RegInit(VecInit(Seq.fill(tlbSize)(false.B)))


  for(i <- 0 until numOfReadPorts) {
    val hitWire = VecInit((0 until tlbSize).map(j => {
      val entry = physicalTLB.read(j.U).asTypeOf(new TLBEntry)
      valid(j) && entry.vpn2 === io.query(i).vAddr(19,1) && (entry.asid === io.asid || entry.global)
    }))
    val indexTLB = hitWire.indexWhere((hit: Bool) => hit)
    val mapped =
      !io.query(i).vAddr(19) || io.query(i).vAddr(19, 18) === "b11".U
    io.result(i).hit := hitWire.reduce(_ || _)
    io.result(i).pageInfo := physicalTLB.read(indexTLB).asTypeOf(new TLBEntry).pages(io.query(i).vAddr(0))
    when(!mapped){
      io.result(i).pageInfo.pfn := Cat(0.U(3.W), io.query(i).vAddr(16,0))
    }
    io.result(i).mapped := mapped
    io.result(i).uncached := ((io.query(i).vAddr(19, 17) === "b101".U) ||
      (io.query(i).vAddr(19, 17) === "b100".U && io.kseg0Uncached) ||
      (mapped && io.result(i).pageInfo.cacheControl === "b010".U))
  }

  val probeWire = VecInit((0 until tlbSize).map(i => {
    val entry = physicalTLB.read(i.U).asTypeOf(new TLBEntry)
    valid(i) && entry.vpn2 === io.probeReq && (entry.global || entry.asid === io.asid)
  }))
  io.probeResp := Cat(
    !probeWire.reduce(_ || _),
    0.U((32 - log2Ceil(tlbSize) - 1).W),
    probeWire.indexWhere((hit: Bool) => hit)
  )

  when(io.instrReq.writeEn) {
    physicalTLB.write(io.instrReq.TLBIndex, io.instrReq.writeData.asUInt())
    valid(io.instrReq.TLBIndex) := true.B
  }
  io.readResp := physicalTLB.read(io.instrReq.TLBIndex).asTypeOf(new TLBEntry)
}
