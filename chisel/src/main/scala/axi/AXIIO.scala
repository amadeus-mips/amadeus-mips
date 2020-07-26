// See README.md for license details.

package axi

import chisel3._
import chisel3.util.DecoupledIO

class AXIMasterIO(dataWidth: Int = 32, addrWidth: Int = 32) extends Bundle {

  /** 读请求地址通道 */
  val ar = new DecoupledIO(new AXIAddrBundle(addrWidth))

  /** 读请求数据通道 */
  val r = Flipped(new DecoupledIO(new AXIDataReadBundle(dataWidth)))

  /** 写请求地址通道 */
  val aw = new DecoupledIO(new AXIAddrBundle(addrWidth))

  /** 写请求数据通道 */
  val w = new DecoupledIO(new AXIDataWriteBundle(dataWidth))

  /** 写请求响应通道 */
  val b = Flipped(new DecoupledIO(new AXIDataWriteRespBundle))

  override def cloneType: AXIMasterIO.this.type = new AXIMasterIO(dataWidth, addrWidth).asInstanceOf[this.type]
}

object AXIIO {
  def master(dataWidth: Int = 32, addrWidth: Int = 32): AXIMasterIO = new AXIMasterIO(dataWidth, addrWidth)
  def slave(dataWidth:  Int = 32, addrWidth: Int = 32): AXIMasterIO = Flipped(new AXIMasterIO(dataWidth, addrWidth))
  def arChannel(addrWidth: Int = 32): AXIAddrBundle= new AXIAddrBundle(addrWidth)
  def rChannel(dataWidth:  Int = 32): AXIDataReadBundle = new AXIDataReadBundle(dataWidth)
  def awChannel(dataWidth:  Int = 32, addrWidth: Int = 32): DecoupledIO[AXIAddrBundle] = new AXIMasterIO(dataWidth, addrWidth).aw
  def wChannel(dataWidth:  Int = 32, addrWidth: Int = 32): DecoupledIO[AXIDataWriteBundle] = new AXIMasterIO(dataWidth, addrWidth).w
  def bChannel(dataWidth:  Int = 32, addrWidth: Int = 32): DecoupledIO[AXIDataWriteRespBundle] = new AXIMasterIO(dataWidth, addrWidth).b
}

class AXIDataWriteRespBundle extends Bundle {
  val id   = UInt(4.W) // ID号，同一请求的bid、wid和awid应一致
  val resp = UInt(2.W) // 本次写请求是否成功完成 (原子访问是否成功）
}

class AXIDataWriteBundle(dataWidth: Int = 32) extends Bundle {
  val id   = UInt(4.W) // ID号 固定为1(仅数据)
  val data = UInt(dataWidth.W) // 写数据
  val strb = UInt(4.W) // 字节选通位
  val last = Bool() // 本次写请求的最后一拍数据的指示信号 固定为1

  override def cloneType: AXIDataWriteBundle.this.type = new AXIDataWriteBundle(dataWidth).asInstanceOf[this.type]
}

class AXIDataReadBundle(dataWidth: Int = 32) extends Bundle {
  val id   = UInt(4.W) // ID 同一请求的rid应和arid一致  指令回来为0数据 回来为1
  val data = UInt(dataWidth.W) // 读回数据
  val resp = UInt(2.W) // 本次读请求是否成功完成 (原子访问是否成功）
  val last = Bool() // 本次读请求的最后一拍数据的指示信号

  override def cloneType: AXIDataReadBundle.this.type = new AXIDataReadBundle(dataWidth).asInstanceOf[this.type]
}

class AXIAddrBundle(addrWidth: Int = 32) extends Bundle {
  val id    = Output(UInt(4.W)) // ID 取指为0 取数为1
  val addr  = Output(UInt(addrWidth.W)) // 地址
  val len   = Output(UInt(4.W)) // 传输的长度(数据传输拍数) 固定为0
  val size  = Output(UInt(3.W)) // 传输的大小(数据传输每拍的字节数)
  val burst = Output(UInt(2.W)) // 传输类型  固定为2’b01
  val lock  = Output(UInt(2.W)) // 原子锁  固定为0
  val cache = Output(UInt(4.W)) // CACHE属性 固定为0
  val prot  = Output(UInt(3.W)) // 保护属性 固定为0

  override def cloneType: AXIAddrBundle.this.type = new AXIAddrBundle(addrWidth).asInstanceOf[this.type]
}
