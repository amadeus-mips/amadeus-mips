package cpu.pipelinedCache.instCache.query

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.{AXIARPort, AXIRPort}
import cpu.pipelinedCache.components._
import cpu.pipelinedCache.instCache.fetch.{ICacheFetchQueryBundle, WriteTagValidBundle}
import shared.Constants.INST_ID
import shared.LRU.{PLRUMRUNM, TrueLRUNM}

@chiselName
class QueryTop(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    // control path IO
    val flush = Input(Bool())
    val ready = Output(Bool())
    // datapath IO
    val fetchQuery = Input(new ICacheFetchQueryBundle)
    val bankData   = Input(Vec(cacheConfig.numOfWays, UInt((cacheConfig.bankWidth * 8).W)))
    val data       = Decoupled(UInt(32.W))
    // also data path, just write to instruction banks
    val write = Valid(new WriteTagValidBundle)

    val instructionWriteBack = Output(Vec(cacheConfig.numOfBanks, UInt((cacheConfig.bankWidth * 8).W)))

    /** is query handling a miss and preparing for write back? */
    val inAMiss = Output(Bool())
    // axi port wiring
    val axi = AXIIO.master()
  })
  // declare all the modules
  val mshr         = Module(new MSHRQ)
  val comparator   = Module(new MissComparator)
  val axiAR        = Module(new AXIARPort(addrReqWidth = 32, AXIID = INST_ID))
  val axiR         = Module(new AXIRPort(addrReqWidth = 32, AXIID = INST_ID))
  val refillBuffer = Module(new ReFillBuffer)
  val queryQueue   = Module(new AddressQueryQueue)
  val lru =
    if (cacheConfig.numOfWays > 2) PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
    else TrueLRUNM(numOfSets                           = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val readHolder = Module(new ReadHolder)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid || io.flush)

  /** is the query a hit in the bank */
  val hitInBank = Wire(Bool())

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer = WireDefault(comparator.io.addrHitInRefillBuffer && refillBuffer.io.queryResult.valid)

  /** if the read holder value is valid, don't generate a new miss  */
  val hitInReadHolder = WireDefault(readHolder.io.output.valid)

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer || hitInReadHolder)

  /** is a new miss generated, and is guaranteed to be accepted */
  val newMiss = Wire(Bool())

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  /** if we write back this cycle */
  val writeBackThisCycle = RegNext(axiR.io.finishTransfer)

  newMiss := !queryHit && !passThrough && mshr.io.recordMiss.ready && !queryQueue.io.queryResult

  /** the next cycle of write back, bank data is invalid, so is tag and valid data */
  hitInBank := comparator.io.bankHitWay.valid && !RegNext(writeBackThisCycle)

  /** io parts */

  io.ready      := io.data.fire || passThrough
  io.data.valid := validData
  io.data.bits := MuxCase(
    io.bankData(comparator.io.bankHitWay.bits),
    Seq(
      readHolder.io.output.valid -> readHolder.io.output.bits,
      hitInRefillBuffer          -> refillBuffer.io.queryResult.bits
    )
  )

  io.inAMiss := mshr.io.pendingMiss

  io.write.valid               := writeBackThisCycle
  io.write.bits.waySelection   := lru.getLRU(mshr.io.writeBackInfo.bits.index)
  io.write.bits.indexSelection := mshr.io.writeBackInfo.bits.index
  io.write.bits.tagValid.tag   := mshr.io.writeBackInfo.bits.tag
  io.write.bits.tagValid.valid := true.B
  io.instructionWriteBack      := refillBuffer.io.allData

  io.axi    := DontCare
  io.axi.ar <> axiAR.io.ar
  io.axi.r  <> axiR.io.r

  comparator.io.tagValid := io.fetchQuery.tagValid
  comparator.io.phyTag   := io.fetchQuery.phyTag
  comparator.io.index    := io.fetchQuery.index
  comparator.io.mshr     := mshr.io.writeBackInfo.bits

  // when is in miss
  refillBuffer.io.queryBankIndex := io.fetchQuery.bankIndex
  refillBuffer.io.newRefillBankIndex := queryQueue.io.dequeue.bits.bankIndex
  refillBuffer.io.inputData <> axiR.io.transferData
  refillBuffer.io.finish    := axiR.io.finishTransfer

  readHolder.io.input.valid := validData && !io.data.ready
  readHolder.io.input.bits := MuxCase(
    readHolder.io.output.bits,
    Seq(
      hitInRefillBuffer                                               -> refillBuffer.io.queryResult.bits,
      (comparator.io.bankHitWay.valid && !readHolder.io.output.valid) -> io.bankData(comparator.io.bankHitWay.bits)
    )
  )

  axiAR.io.addrReq.bits := Mux(
    newMiss,
    Cat(
      io.fetchQuery.phyTag,
      io.fetchQuery.index,
      io.fetchQuery.bankIndex,
      0.U(cacheConfig.bankOffsetLen.W)
    ),
    Cat(mshr.io.extractMiss.asUInt, 0.U(cacheConfig.bankOffsetLen.W))
  )
  axiAR.io.addrReq.valid := newMiss

  /** if there is a legitimate miss, i.e., valid request, didn't hit, and is not flushed */
  mshr.io.recordMiss.valid          := newMiss
  mshr.io.recordMiss.bits.tag       := io.fetchQuery.phyTag
  mshr.io.recordMiss.bits.index     := io.fetchQuery.index
  mshr.io.recordMiss.bits.bankIndex := io.fetchQuery.bankIndex
  mshr.io.writeBackInfo.ready       := writeBackThisCycle
  mshr.io.arComplete                := axiAR.io.arCommit

  queryQueue.io.enqueue.valid          := newMiss
  queryQueue.io.enqueue.bits.tag       := io.fetchQuery.phyTag
  queryQueue.io.enqueue.bits.index     := io.fetchQuery.index
  queryQueue.io.enqueue.bits.bankIndex := io.fetchQuery.bankIndex
  queryQueue.io.queryAddress.tag       := io.fetchQuery.phyTag
  queryQueue.io.queryAddress.index     := io.fetchQuery.index
  queryQueue.io.queryAddress.bankIndex := io.fetchQuery.bankIndex
  queryQueue.io.dequeue.ready          := axiR.io.finishTransfer

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
