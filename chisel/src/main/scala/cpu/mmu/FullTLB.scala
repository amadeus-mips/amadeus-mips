package cpu.mmu

import chisel3._
import chisel3.util._

/**
  * this is a TLB that only translate the upper bits
  *
  * @param numOfReadPorts how many read ports there are
  * @param vAddrWidth     how wide the virtual address is ( excluding bits that won't be translated )
  * @param TLBSize        how many entries are in TLB
  * @param phyAddrWidth   how wide the translated physical address is
  */
class FullTLB(numOfReadPorts: Int, vAddrWidth: Int, TLBSize: Int, phyAddrWidth: Int)
  extends Module {
  val io = IO(new Bundle {
    val query = Input(Vec(numOfReadPorts, new TLBQuery(vAddrWidth)))
    val result = Output(Vec(numOfReadPorts, new TLBResult(TLBSize, phyAddrWidth)))

    // there should be only 1 operation port that can handle both read and write
    val instrReq = Input(new TLBRWReq(TLBSize, phyAddrWidth))
    val readResp = Output(new TLBEntry(phyAddrWidth))

    // the probing instruction
    val probeReq = Input(UInt(vAddrWidth.W))
    val probeResp = Output(UInt(32.W))
  })

  val physicalTLB = RegInit(VecInit(Seq.fill(TLBSize)(new TLBEntry(phyAddrWidth))))

  // read port for I-cache, (D-cache, uncached)
  // NOTE: query is executed regardless of whether the input is valid, validity of input
  // will only be judged by outer circuit
  val hitWire = Wire(Vec(numOfReadPorts, Vec(TLBSize, Bool())))
  val hitIndex = Wire(Vec(numOfReadPorts, UInt(log2Ceil(TLBSize).W)))
  for (i <- 0 until numOfReadPorts) {
    hitWire(i) := physicalTLB.map((entry: TLBEntry) =>
      (entry.vpn2 === io.query(i).vAddr(vAddrWidth - 1, 1)) && ((entry.asid === io.query(i).asid) || (entry.global))
    )
    val isHit = hitWire(i).contains(true.B)
    val indexTLB = hitWire(i).indexWhere((hit: Bool) => hit)
    val page = physicalTLB(hitIndex(i)).pages(io.query(i).vAddr(0))
    val mapped = page.cacheControl === 2.U
    val cached = page.cacheControl === 3.U
    //    val untranslated = io.query(i).vAddr(19, 18) === "b10".U(2.W) || io.query(i).vAddr(19, 16) === "b1100".U(4.W)
    hitIndex(i) := indexTLB
    io.result(i).hit := isHit
    io.result(i).pageInfo := page
    when(!mapped) {
      io.result(i).pageInfo.pfn := io.query(i).vAddr
    }
    io.result(i).mapped := mapped
    io.result(i).cached := cached
  }

  // the probe request and response
  val probeWire = Wire(Vec(TLBSize, Bool()))
  probeWire := physicalTLB map ((entry: TLBEntry) => (entry.vpn2 === io.probeReq(vAddrWidth - 1, 1)))
  io.probeResp := Cat(!probeWire.contains(true.B), 0.U((32 - log2Ceil(TLBSize) - 1).W), probeWire.indexWhere((hit: Bool) => hit))

  // the read and write request
  when(io.instrReq.writeEn) {
    physicalTLB(io.instrReq.TLBIndex) := io.instrReq.writeData
  }
  io.readResp := physicalTLB(io.instrReq.TLBIndex)
}
