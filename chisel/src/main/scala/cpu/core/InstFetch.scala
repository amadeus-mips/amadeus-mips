// See README.md for license details.

package cpu.core

import chisel3._
import cpu.common.DefaultWireLength
import cpu.core.bundles.IFIDBundle

object InstFetch {
  val startPC = "hbfbffffc".U(32.W)
}

class InstFetch extends Module with DefaultWireLength {
  val io = IO(new Bundle {
    // from ctrl module
    val stall = Input(UInt(cpuStallLen.W))
    val flush = Input(Bool())
    val flushPC = Input(UInt(addressLen.W))

    // from id module
    val branchFlag = Input(Bool())
    val branchTarget = Input(UInt(addressLen.W))

    // from ram
    val inst = Input(UInt(dataLen.W))
    val instValid = Input(Bool())

    // to IFID
    val out = Output(new IFIDBundle)
    // to ram
    val outputPCValid = Output(Bool())
    // to ctrl
    val outputStallReq = Output(Bool())
  })

  /**
   * 记录是否发生指令地址(pc)未对齐异常
   */
  val instFetchExcept = RegInit(false.B)

  val pc = RegInit(InstFetch.startPC)
  val pcValid = RegInit(false.B)

  /**
   * 是否需要暂停流水线，
   * 若pc有效但指令无效则请求暂停流水线
   */
  val stallReq = pcValid && !io.instValid

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
  instBuffer := Mux(stalledByOthers && !useIBuffer, io.inst, instBuffer)

  io.out.instFetchExcept := instFetchExcept
  io.out.pc := pc
  io.out.inst := Mux(useIBuffer, instBuffer, io.inst)
  io.outputPCValid := pcValid
  io.outputStallReq := stallReq

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
    pc := io.flushPC
    pcValid := !addressMisalignment(io.flushPC)
    instFetchExcept := addressMisalignment(io.flushPC)
  }.elsewhen(!io.stall(0)) {
    // 流水线未暂停，pc+=4，或为跳转指令指定的pc
    // 非跳转指令默认不会产生指令地址未对齐异常
    pc := Mux(io.branchFlag, io.branchTarget, pc + 4.U)
    pcValid := !io.branchFlag || !addressMisalignment(io.branchTarget)
    instFetchExcept := io.branchFlag && addressMisalignment(io.branchTarget)
  }.otherwise {
    // 流水线被暂停
    pc := pc
    pcValid := stallReq
    instFetchExcept := false.B
  }
}
