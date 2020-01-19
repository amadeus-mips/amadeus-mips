// See README.md for license details.

package cpu.core

import chisel3._

object InstFetch {
  val startPC = "hbfbffffc".U(32.W)
}

class InstFetch extends Module {
  val io = IO(new Bundle {
    // from ctrl module
    val stall = Input(UInt(6.W))
    val flush = Input(Bool())
    val flushPC = Input(UInt(32.W))

    // from id module
    val branchFlag = Input(Bool())
    val branchTargetAddress = Input(UInt(32.W))

    // from ram
    val inst = Input(UInt(32.W))
    val instValid = Input(Bool())

    val outputInstFetchExcept = Output(Bool())

    // to ram and if_id
    val outputPC = Output(UInt(32.W))
    // to ram
    val outputPCValid = Output(Bool())

    val outputInst = Output(UInt(32.W))
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
  val instBuffer = RegInit(0.U(32.W))
  /**
   * see [[instBuffer]]
   */
  val useIBuffer = RegInit(false.B)

  io.outputInstFetchExcept := instFetchExcept
  io.outputPC := pc
  io.outputPCValid := pcValid
  io.outputInst := Mux(useIBuffer, instBuffer, io.inst)
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
    pc := Mux(io.branchFlag, io.branchTargetAddress, pc + 4.U)
    pcValid := !io.branchFlag || !addressMisalignment(io.branchTargetAddress)
    instFetchExcept := io.branchFlag && addressMisalignment(io.branchTargetAddress)
  }.otherwise {
    // 流水线被暂停
    pc := pc
    pcValid := stallReq
    instFetchExcept := false.B
  }
  useIBuffer := stalledByOthers
  instBuffer := Mux(stalledByOthers && !useIBuffer, io.inst, instBuffer)
}
