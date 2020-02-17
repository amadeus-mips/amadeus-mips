// See README.md for license details.

package cpu.core.fetch

import chisel3._
import chisel3.util.ValidIO
import cpu.core.Constants._
import cpu.core.bundles.ValidBundle
import cpu.core.bundles.stage.IFIDBundle

class Fetch extends Module {
  val io = IO(new Bundle {
    // from ctrl module
    val stall = Input(UInt(cpuStallLen.W))
    val flush = Input(Bool())
    val flushPC = Input(UInt(addrLen.W))

    // from id module
    val branch = Input(new ValidBundle)

    // from ram
    val inst = Input(new ValidBundle)

    // to IFID
    val out = Output(new IFIDBundle)
    // to ram
    val outPCValid = Output(Bool())
    // to ctrl
    val outStallReq = Output(Bool())
  })

  /**
   * 记录是否发生指令地址(pc)未对齐异常
   */
  val instFetchExcept = RegInit(false.B)

  val pc = RegInit({
    val bundle = Wire(new ValidBundle)
    bundle.bits := startPC
    bundle.valid := false.B
    bundle
  })

  /**
   * 是否需要暂停流水线，
   * 若pc有效但指令无效则请求暂停流水线
   */
  val stallReq = pc.valid && !io.inst.valid

  /**
    * 流水线是否被<b>其它模块</b>暂停
    */
  val stalledByOthers = !io.flush && io.stall(0) && !stallReq
  /**
   * 暂存指令<Br/>
   * 由于cache传来的指令不是reg型，不能持续，
   * 若被<b>其它模块</b>暂停则需将读到的指令暂存。
   * TODO: 把这个实现移到ICache中
   */
  val instBuffer = RegInit(0.U(dataLen.W))
  /**
   * see [[instBuffer]]
   */
  val useIBuffer = RegInit(false.B)

  useIBuffer := stalledByOthers
  instBuffer := Mux(stalledByOthers && !useIBuffer, io.inst.bits, instBuffer)

  io.out.instFetchExcept := instFetchExcept
  io.out.pc := pc.bits
  io.out.inst := Mux(useIBuffer, instBuffer, io.inst.bits)
  io.outPCValid := pc.valid
  io.outStallReq := stallReq

  /**
   * 判断是否会发生取指异常
   *
   * @param address 输入地址
   */
  def addressMisalignment(address: UInt): Bool = {
    address(1, 0) =/= 0.U
  }

  when(io.flush) {
    // 刷新流水线
    pc.bits := io.flushPC
    pc.valid := !addressMisalignment(io.flushPC)
    instFetchExcept := addressMisalignment(io.flushPC)
  }.elsewhen(!io.stall(0)) {
    // 流水线未暂停，pc+=4，或为跳转指令指定的pc
    // 非跳转指令默认不会产生指令地址未对齐异常
    pc.bits := Mux(io.branch.valid, io.branch.bits, pc.bits + 4.U)
    pc.valid := !io.branch.valid || !addressMisalignment(io.branch.bits)
    instFetchExcept := io.branch.valid && addressMisalignment(io.branch.bits)
  }.otherwise {
    // 流水线被暂停
    pc.bits := pc.bits
    pc.valid := stallReq
    instFetchExcept := false.B
  }
}
