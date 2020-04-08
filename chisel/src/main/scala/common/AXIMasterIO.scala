// See README.md for license details.

package common

import chisel3._

class AXIMasterIO(dataWidth: Int = 32, addrWidth: Int = 32) extends Bundle {
  /** 读请求地址通道 */
  val ar = new AXIAddrMasterIO(addrWidth)
  /** 读请求数据通道 */
  val r = new AXIDataReadMasterIO(dataWidth)
  /** 写请求地址通道 */
  val aw = new AXIAddrMasterIO(addrWidth)
  /** 写请求数据通道 */
  val w = new AXIDataWriteMasterIO(dataWidth)
  /** 写请求响应通道 */
  val b = new AXIDataWriteRespMasterIO()

  override def cloneType: AXIMasterIO.this.type = new AXIMasterIO(dataWidth, addrWidth).asInstanceOf[this.type ]
}

object AXIIO {
  def master(dataWidth: Int = 32, addrWidth: Int = 32): AXIMasterIO = new AXIMasterIO(dataWidth, addrWidth)
  def slave(dataWidth: Int = 32, addrWidth: Int = 32): AXIMasterIO = Flipped(new AXIMasterIO(dataWidth, addrWidth))
}

class AXIDataWriteRespMasterIO extends Bundle {
  val id = Input(UInt(4.W))   // ID号，同一请求的bid、wid和awid应一致
  val resp = Input(UInt(2.W)) // 本次写请求是否成功完成 (原子访问是否成功）
  val valid = Input(Bool())   // 握手信号，写请求响应有效
  val ready = Output(Bool())  // 握手信号，master端准备好接受写响应
}

class AXIDataWriteMasterIO(dataWidth: Int = 32) extends Bundle {
  val id = Output(UInt(4.W))     // ID号 固定为1(仅数据)
  val data = Output(UInt(dataWidth.W))  // 写数据
  val strb = Output(UInt(4.W))   // 字节选通位
  val last = Output(Bool())      // 本次写请求的最后一拍数据的指示信号 固定为1
  val valid = Output(Bool())     // 写请求数据有效
  val ready = Input(Bool())      // 握手信号，slave端准备好接受数据传输

  override def cloneType: AXIDataWriteMasterIO.this.type = new AXIDataWriteMasterIO(dataWidth).asInstanceOf[this.type ]
}

class AXIDataReadMasterIO(dataWidth: Int = 32) extends Bundle {
  val id = Input(UInt(4.W))      // ID 同一请求的rid应和arid一致  指令回来为0数据 回来为1
  val data = Input(UInt(dataWidth.W))   // 读回数据
  val resp = Input(UInt(2.W))    // 本次读请求是否成功完成 (原子访问是否成功）
  val last = Input(Bool())       // 本次读请求的最后一拍数据的指示信号
  val valid = Input(Bool())      // 握手信号，读请求数据有效
  val ready = Output(Bool())     // 握手信号，master端准备好接受数据传输

  override def cloneType: AXIDataReadMasterIO.this.type = new AXIDataReadMasterIO(dataWidth).asInstanceOf[this.type ]
}

class AXIAddrMasterIO(addrWidth: Int = 32) extends Bundle {
  val id = Output(UInt(4.W))    // ID 取指为0 取数为1
  val addr = Output(UInt(addrWidth.W)) // 地址
  val len = Output(UInt(4.W))   // 传输的长度(数据传输拍数) 固定为0
  val size = Output(UInt(3.W))  // 传输的大小(数据传输每拍的字节数)
  val burst = Output(UInt(2.W)) // 传输类型  固定为2’b01
  val lock = Output(UInt(2.W))  // 原子锁  固定为0
  val cache = Output(UInt(4.W)) // CACHE属性 固定为0
  val prot = Output(UInt(3.W))  // 保护属性 固定为0
  val valid = Output(Bool())    // 握手信号，地址有效
  val ready = Input(Bool())     // 握手信号，slave端准备好接受地址传输

  override def cloneType: AXIAddrMasterIO.this.type = new AXIAddrMasterIO(addrWidth).asInstanceOf[this.type ]
}
