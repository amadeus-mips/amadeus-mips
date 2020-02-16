package common

import chisel3._
import chisel3.util._

object Util {
  def listHasElement(list: Seq[UInt], element: UInt): Bool = {
    list.foldLeft(true.B)((r, e) => r || (e === element))
  }

  def unsignedToSigned(s: BigInt, width: Int = 32): BigInt = {
    val m = Limits.MAXnBIT(width - 1)
    if (s >= m) s - 2 * m
    else s
  }

  def signedExtend(raw: UInt, to: Int = 32): UInt = {
    signedExtend(raw, raw.getWidth, to)
  }

  def signedExtend(raw: UInt, from: Int, to: Int): UInt = {
    require(to > from && from >= 1)
    Cat(Fill(to - from, raw(from - 1)), raw)
  }

  def zeroExtend(raw: UInt, to: Int = 32): UInt = {
    zeroExtend(raw, raw.getWidth, to)
  }

  def zeroExtend(raw: UInt, from: Int, to: Int): UInt = {
    require(to > from && from >= 1)
    Cat(Fill(to - from, 0.U), raw)
  }
}