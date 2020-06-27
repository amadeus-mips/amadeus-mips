package cpu.pipelinedCache.components

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._

/**
  * refill buffer to temporarily holds the returned data until AXI transfer is complete
  *
  * @param tagWidth    how wide is the tag in the address
  * @param indexWidth  how wide is the index in the address
  * @param wordAmount  how many words(4 bytes) are there in 1 line ( refill buffer )
  * @param writeEnable whether this can be used for D-cache
  */
@chiselName
class ReFillBuffer(tagWidth: Int = 20, indexWidth: Int, wordAmount: Int, writeEnable: Boolean = false) {

  val bufferValid = RegInit(false.B)
  val buffer = RegInit(VecInit(Seq.fill(wordAmount)(if (writeEnable) VecInit(Seq.fill(4)(0.U(8.W))) else 0.U(32.W))))
  val bufferValidMask = RegInit(VecInit(Seq.fill(wordAmount)(false.B)))
  val writePtr = Reg(UInt(log2Ceil(wordAmount).W))

  val tagReg = Reg(UInt(tagWidth.W))
  val indexReg = Reg(UInt(indexWidth.W))
  val endOffset = Reg(UInt(log2Ceil(wordAmount).W))

  /**
    * start a new refill session
    *
    * @param tag    tag of the cache miss address
    * @param index  index of the cache miss address
    * @param offset offset of the cache miss address
    */
  def init(tag: UInt, index: UInt, offset: UInt): Unit = {
    require(tag.getWidth == tagWidth, "input tag and tag register should have the same length")
    require(index.getWidth == indexWidth, "input index and index register should have the same length")
    require(offset.getWidth == log2Ceil(wordAmount), "input offset and offset register should have the same length")
    bufferValid := true.B
    writePtr := offset
    endOffset := offset - 1.U
    tagReg := tag
    indexReg := index
    invalidate()
  }

  /**
    * a read query to test if a read request has a hit
    *
    * @param tag    tag part of the address
    * @param index  index part of the address
    * @param offset offset part of the address
    * @return the data at the offset, and whether the returned data is a hit
    */
  def query(tag: UInt, index: UInt, offset: UInt): (UInt, Bool) = {
    (
      buffer(offset).asTypeOf(UInt(32.W)),
      bufferValid && bufferValidMask(offset) && tag === tagReg && index === indexReg
    )
  }

  /**
    * refill a new portion of data
    *
    * @param data data to write into refill buffer
    */
  def refill(data: UInt): Unit = {
    buffer(writePtr) := data
    bufferValid(writePtr) := true.B
    writePtr := writePtr + 1.U
  }

  /**
    * write back the whole refill buffer to cache
    *
    * @return the full contents of refill buffer
    */
  def getAll: Vec[UInt] = {
    invalidate()
    buffer.asTypeOf(Vec(wordAmount, UInt(32.W)))
  }

  /**
    * invalidate the refill buffer in case it wasn't
    */
  def invalidate(): Unit = {
    assert(bufferValidMask.reduceTree(_ & _), "buffer should be valid, but not written back")
    bufferValidMask := 0.U.asTypeOf(bufferValidMask)
    bufferValid := false.B
  }

  /**
    * is the process of refilling complete
    *
    * @return true.B if refilling is complete
    */
  def isFull: Bool = {
    writePtr === endOffset
  }

  /**
    * whether refill buffer is ready for another fill
    *
    * @return true.b is ready, false.b is not
    */
  def ready: Bool = {
    !bufferValid
  }
}
