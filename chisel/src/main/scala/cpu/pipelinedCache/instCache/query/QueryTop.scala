package cpu.pipelinedCache.instCache.query

import axi.AXIIO
import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import cpu.pipelinedCache.CacheConfig
import cpu.pipelinedCache.components.AXIPorts.AXIReadPort
import cpu.pipelinedCache.components.{MSHR, MissComparator, ReFillBuffer, ReadHolder}
import cpu.pipelinedCache.instCache.fetch.{ICacheFetchQueryBundle, WriteTagValidBundle}
import shared.Constants.INST_ID
import shared.LRU.PLRUMRUNM

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
  val mshr         = Module(new MSHR)
  val comparator   = Module(new MissComparator)
  val axi          = Module(new AXIReadPort(addrReqWidth = 32, AXIID = INST_ID))
  val refillBuffer = Module(new ReFillBuffer)
  val lru          = PLRUMRUNM(numOfSets = cacheConfig.numOfSets, numOfWay = cacheConfig.numOfWays)
  val readHolder   = Module(new ReadHolder)

  /** do nothing to this query, proceed to next */
  val passThrough = WireDefault(!io.fetchQuery.valid || io.flush)

  /** is the query a hit in the bank */
  val hitInBank = WireDefault(comparator.io.bankHitWay.valid)

  /** is the query hit in the refill buffer */
  val hitInRefillBuffer = WireDefault(comparator.io.hitInRefillBuffer)

  /** is the query a hit in either places */
  val queryHit = WireDefault(hitInBank || hitInRefillBuffer)

  /** is a new miss generated, but is not guaranteed to be accepted */
  val newMiss = WireDefault(!queryHit && !passThrough)

  /** is the data.valid output high? */
  val validData = WireDefault(queryHit && !passThrough)

  val qIdle :: qRefill :: qWriteBack :: Nil = Enum(3)
  val qState                                = RegInit(qIdle)
  switch(qState) {
    is(qIdle) {
      when(newMiss) {
        qState := qRefill
      }
    }
    is(qRefill) {
      when(axi.io.finishTransfer) {
        qState := qWriteBack
      }
    }
    is(qWriteBack) {
      qState := Mux(newMiss, qRefill, qIdle)
    }
  }

  /** io parts */

  io.ready      := io.data.fire || passThrough
  io.data.valid := validData
  io.data.bits := MuxCase(
    io.bankData(comparator.io.bankHitWay.bits),
    Seq(
      readHolder.io.output.valid      -> readHolder.io.output.bits,
      comparator.io.hitInRefillBuffer -> refillBuffer.io.queryResult.bits
    )
  )

  io.inAMiss := qState === qRefill

  io.write.valid               := qState === qWriteBack
  io.write.bits.waySelection   := lru.getLRU(mshr.io.mshrInfo.index)
  io.write.bits.indexSelection := mshr.io.mshrInfo.index
  io.write.bits.tagValid.tag   := mshr.io.mshrInfo.tag
  io.write.bits.tagValid.valid := true.B
  io.instructionWriteBack      := refillBuffer.io.allData

  io.axi := DontCare
  io.axi <> axi.io.axi

  comparator.io.tagValid          := io.fetchQuery.tagValid
  comparator.io.phyTag            := io.fetchQuery.phyTag
  comparator.io.index             := io.fetchQuery.index
  comparator.io.mshr              := mshr.io.mshrInfo
  comparator.io.refillBufferValid := refillBuffer.io.queryResult.valid

  // when is in miss
  refillBuffer.io.bankIndex.valid := newMiss || (qState === qRefill && !refillBuffer.io.finish)
  refillBuffer.io.bankIndex.bits  := io.fetchQuery.bankIndex
  refillBuffer.io.inputData       := axi.io.transferData
  refillBuffer.io.finish          := axi.io.finishTransfer

  readHolder.io.input.valid := validData && !io.data.ready
  readHolder.io.input.bits := MuxCase(
    readHolder.io.output.bits,
    Seq(
      comparator.io.hitInRefillBuffer                                 -> refillBuffer.io.queryResult.bits,
      (comparator.io.bankHitWay.valid && !readHolder.io.output.valid) -> io.bankData(comparator.io.bankHitWay.bits)
    )
  )

  axi.io.addrReq.bits := Mux(
    newMiss,
    Cat(
      io.fetchQuery.phyTag,
      io.fetchQuery.index,
      io.fetchQuery.bankIndex,
      0.U(cacheConfig.bankOffsetLen.W)
    ),
    Cat(mshr.io.mshrInfo.asUInt, 0.U(cacheConfig.bankOffsetLen.W))
  )
  axi.io.addrReq.valid := newMiss

  /** if there is a legitimate miss, i.e., valid request, didn't hit, and is not flushed */
  mshr.io.missAddr.valid          := newMiss && qState =/= qRefill
  mshr.io.missAddr.bits.tag       := io.fetchQuery.phyTag
  mshr.io.missAddr.bits.index     := io.fetchQuery.index
  mshr.io.missAddr.bits.bankIndex := io.fetchQuery.bankIndex

  // update the LRU when there is a hit in the banks, don't update otherwise
  when(hitInBank) {
    lru.update(index = io.fetchQuery.index, way = comparator.io.bankHitWay.bits)
  }
}
