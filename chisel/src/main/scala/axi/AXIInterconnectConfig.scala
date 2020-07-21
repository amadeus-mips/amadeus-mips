/*

Copyright (c) 2018 Alex Forencich

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Modified from
https://github.com/alexforencich/verilog-axi/blob/master/rtl/axi_interconnect.v
by Discreater
*/

package axi

import chisel3._
import chisel3.util.log2Ceil
import spire.math.Natural

/**
  * example see the Object of [[AXIInterconnectConfig]]
  * @param sCount        Number of AXI inputs (slave interfaces)
  * @param mCount        Number of AXI outputs (master interfaces)
  * @param dataWidth     Width of data bus in bits
  * @param addrWidth     Width of request bus in bits
  * @param idWidth       Width of ID signal
  * @param awUserEnable  Propagate awuser signal
  * @param awUserWidth   Width of awuser signal
  * @param wUserEnable   Propagate wuser signal
  * @param wUserWidth    Width of wuser signal
  * @param bUserEnable   Propagate buser signal
  * @param bUserWidth    Width of buser signal
  * @param arUserEnable  Propagate aruser signal
  * @param arUserWidth   Width of aruser signal
  * @param rUserEnable   Propagate ruser signal
  * @param rUserWidth    Width of ruser signal
  * @param forwardID     Propagate ID field
  * @param mRegions      Number of regions per master interface
  * @param mBaseAddr     Master interface base addresses
  *                      M_COUNT concatenated fields of M_REGIONS concatenated fields of ADDR_WIDTH bits
  *                      set to zero for default addressing based on M_ADDR_WIDTH
  * @param mAddrWidth    Master interface request widths
  *                      M_COUNT concatenated fields of M_REGIONS concatenated fields of 32 bits
  * @param mConnectRead_raw  Read connections between interfaces
  *                      M_COUNT concatenated fields of S_COUNT bits
  * @param mConnectWrite_raw Write connections between interfaces
  *                      M_COUNT concatenated fields of S_COUNT bits
  * @param mSecure       Secure master (fail operations based on awprot/arprot)
  *                      M_COUNT bits
  * @param mDefaultMatch default master interface, set to -1 to disable
  */
class AXIInterconnectConfig
(
  val sCount: Int = 4,
  val mCount: Int = 4,
  val dataWidth: Int = 32,
  val addrWidth: Int = 32,
  val idWidth: Int = 8,
  val awUserEnable: Boolean = false,
  val awUserWidth: Int = 1,
  val wUserEnable: Boolean = false,
  val wUserWidth: Int = 1,
  val bUserEnable: Boolean = false,
  val bUserWidth: Int = 1,
  val arUserEnable: Boolean = false,
  val arUserWidth: Int = 1,
  val rUserEnable: Boolean = false,
  val rUserWidth: Int = 1,
  val forwardID: Boolean = false,
  val mRegions: Int = 1,
  val mBaseAddr: Seq[Seq[UInt]],
  val mAddrWidth: Seq[Seq[Int]],
  mConnectRead_raw: Seq[Seq[Bool]],
  mConnectWrite_raw: Seq[Seq[Bool]],
  val mSecure: Seq[Boolean],
  val mDefaultMatch: Int = -1,
  banLog: Boolean = false,
) {
  def strbWidth = dataWidth / 8
  def clsCount = log2Ceil(sCount)
  def clmCount = log2Ceil(mCount)
  def mConnectRead: Vec[Vec[Bool]] = {
    VecInit(mConnectRead_raw.map(s => VecInit(s)))
  }
  def mConnectWrite: Vec[Vec[Bool]] = {
    VecInit(mConnectWrite_raw.map(s => VecInit(s)))
  }

  def aUserWidth = if (awUserWidth > arUserWidth) awUserWidth else arUserWidth

  private def err(msg: String) = {
    println(Console.RED + "Error: " + msg + Console.RESET)
  }

  private def info(msg: String) = {
    if (!banLog)
      println(Console.CYAN + "Info: " + msg + Console.RESET)
  }

  //noinspection DuplicatedCode
  private def configureCheck: Boolean = {
    var flag = true
    if(mDefaultMatch != -1 && (mDefaultMatch < 0 || mDefaultMatch >= mRegions)){
      flag = false
    }
    if (mRegions < 1 || mRegions > 16) {
      err(s"M_REGIONS is $mRegions, must be between 1 and 16 (instance %m)")
      flag = false
    }

    for (i <- 0 until mCount) {
      for (j <- 0 until mRegions) {
        if (mAddrWidth(i)(j) != 0 && mAddrWidth(i)(j) < 12 || mAddrWidth(i)(j) > addrWidth) {
          err(s"request is ${mAddrWidth(i)(j)} width out of range (instance %m)")
          flag = false
        }
      }
    }

    val max = (Natural(1) << addrWidth) - Natural(1)

    def regionInfo(i: Int, j: Int, mba: BigInt, maw: Int, start: BigInt, end: BigInt): String = {
      val fx = s"%0${addrWidth / 4}x"
      s"%2d (%2d): $fx / %2d -- $fx-$fx".format(i, j, mba, maw, start, end)
    }

    info("Addressing configuration for axi_interconnect instance %m")
    for (i <- 0 until mCount) {
      for (j <- 0 until mRegions) {
        val mba = mBaseAddr(i)(j).litValue()
        val maw = mAddrWidth(i)(j)
        if (maw != 0) {
          val mbau = Natural(mba)
          val start = (mbau & (max << maw)).toBigInt
          val end = (mbau | (max >> (addrWidth - maw))).toBigInt
          info(regionInfo(i, j, mba, maw, start, end))
        }
      }
    }

    for (i <- 0 until mCount) {
      for (j <- 0 until mRegions) {
        for (ii <- i until mCount) {
          for (jj <- j + 1 until mRegions) {
            val mawa = mAddrWidth(i)(j)
            val mawb = mAddrWidth(ii)(jj)
            if (mawa != 0 && mawb != 0) {
              val mbaua = Natural(mBaseAddr(i)(j).litValue())
              val starta = (mbaua & (max << mawa)).toBigInt
              val enda = (mbaua | (max >> (addrWidth - mawb))).toBigInt
              val mbaub = Natural(mBaseAddr(ii)(jj).litValue())
              val startb = (mbaub & (max << mawb)).toBigInt
              val endb = (mbaub | (max >> (addrWidth - mawb))).toBigInt

              if ((starta <= endb) && (startb <= enda)) {
                err("Overlapping regions:")
                err(regionInfo(i, j, mbaua.toBigInt, mawa, starta, enda))
                err(regionInfo(ii, jj, mbaub.toBigInt, mawb, startb, endb))
                err("request range overlap (instance %m")
              }
            }
          }
        }
      }
    }
    flag
  }

  require(configureCheck)

  def this
  (
    sCount: Int,
    mCount: Int,
    idWidth: Int,
    forwardID: Boolean,
    mRegions: Int,
    mBaseAddr: Seq[Seq[UInt]],
    mAddrWidth: Seq[Seq[Int]],
    mDefaultMatch: Int,
  ) {
    this(
      sCount = sCount,
      mCount = mCount,
      idWidth = idWidth,
      forwardID = forwardID,
      mRegions = mRegions,
      mBaseAddr = mBaseAddr,
      mAddrWidth = mAddrWidth,
      mConnectRead_raw = Seq.fill(mCount)(Seq.fill(sCount)(true.B)),
      mConnectWrite_raw = Seq.fill(mCount)(Seq.fill(sCount)(true.B)),
      mSecure = Seq.fill(mCount)(true),
      mDefaultMatch = mDefaultMatch
    )
  }
}

object AXIInterconnectConfig {


  /**
    * base loongson func and perf test.
    * @param sCount the number of slave interface
    * @param mCount the number of master interface
    * @return
    */
  def loongson_func(sCount: Int = 1, mCount: Int = 2) = new AXIInterconnectConfig(
    sCount = sCount,
    mCount = mCount,
    idWidth = 4,
    forwardID = true,
    mRegions = 5,
    mBaseAddr = Seq(
      Seq("h1fc00000".U(32.W), "h20000000".U(32.W), "h40000000".U(32.W), "h80000000".U(32.W), "h00000000".U(32.W)),
      Seq("h1faf0000".U(32.W), "hffffffff".U(32.W), "hffffffff".U(32.W), "hffffffff".U(32.W), "hffffffff".U(32.W))
    ),
    mAddrWidth = Seq(
      Seq(22, 29, 30, 31, 28),
      Seq(16, 0, 0, 0, 0)
    ),
    mDefaultMatch = -1
  )

  def loongson_system(sCount: Int = 1) = new AXIInterconnectConfig(
    sCount = sCount,
    mCount = 5,
    idWidth = 4,
    forwardID = true,
    mRegions = 2,
    mBaseAddr = Seq(
      Seq("h00000000".U(32.W), "hffffffff".U(32.W)), // ddr3
      Seq("h1fc00000".U(32.W), "h1fe80000".U(32.W)), // SPI(flash
      Seq("h1fe40000".U(32.W), "hffffffff".U(32.W)), // APB(uart)
      Seq("h1faf0000".U(32.W), "hffffffff".U(32.W)), // CONF
      Seq("hffffffff".U(32.W), "hffffffff".U(32.W)), // MAC: unused
    ),
    mAddrWidth = Seq(
      Seq(32, 0),
      Seq(20, 16),
      Seq(16, 0),
      Seq(16, 0),
      Seq(0, 0),
    ),
    mDefaultMatch = -1
  )
}
