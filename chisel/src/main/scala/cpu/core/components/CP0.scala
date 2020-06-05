// See README.md for license details.

package cpu.core.components

import chisel3._
import chisel3.util.{Cat, MuxCase, MuxLookup, log2Ceil}
import cpu.common.CP0Struct
import cpu.core.Constants._
import cpu.core.bundles.CPBundle

class CP0IO extends Bundle {
  val intr        = Input(UInt(intrLen.W))
  val cp0Write    = Input(new CPBundle)
  val addr        = Input(UInt(regAddrLen.W))
  val sel         = Input(UInt(3.W))
  val except      = Input(Vec(exceptAmount, Bool()))
  val inDelaySlot = Input(Bool())
  val pc          = Input(UInt(addrLen.W))
  val badAddr     = Input(UInt(addrLen.W))

  val data = Output(UInt(dataLen.W))

  val status_o = Output(UInt(dataLen.W))
  val cause_o  = Output(UInt(dataLen.W))
  val EPC_o    = Output(UInt(dataLen.W))
}

class CP0(tlbEntries: Int = 32) extends Module {
  val io = IO(new CP0IO)

  val index    = RegInit(0.U(32.W))
  val entryLo0 = RegInit(0.U(32.W))
  val entryLo1 = RegInit(0.U(32.W))
  val pageMask = RegInit(0.U(32.W))
  val badVAddr = RegInit(0.U(32.W))
  val count    = RegInit(0.U(32.W))
  val entryHi  = RegInit(0.U(32.W))
//  val status = RegInit(Cat(0.U(9.W), 1.U(1.W), 0.U(22.W))) ???
  val status = RegInit("h00400000".U(32.W))
  val cause  = RegInit(0.U(32.W))
  val epc    = RegInit(0.U(32.W))

  val cp0Map = Map(
    con_Index    -> index,
    con_EntryLo0 -> entryLo0,
    con_EntryLo1 -> entryLo1,
    con_PageMask -> pageMask,
    con_BadVAddr -> badVAddr,
    con_Count    -> count,
    con_EntryHi  -> entryHi,
    con_Status   -> status,
    con_Cause    -> cause,
    con_EPC      -> epc
  )

  val tick = RegInit(false.B)
  tick := !tick

  val wdata  = io.cp0Write.data
  val except = io.except.asUInt().orR()

  def compareWriteCP0(p: CP0Struct): Bool = {
    val c = io.cp0Write
    c.enable && c.addr === p.addr.U && c.sel === p.sel.U
  }

  /**
    * Get the data from cp0 `p(t._1, t._2)` . If it is writing to `p`, will use the writing data.
    * @param p the source CP0Struct.
    * @param t the interval.
    * @return
    */
  def wtf(p: CP0Struct, t: (Int, Int)): UInt = {
    val c = cp0Map(p)
    require(t._1 > t._2 && t._1 <= 31 && t._2 >= 0)
    Mux(compareWriteCP0(p), wdata(t._1, t._2), c(t._1, t._2))
  }

  /**
    * Get the data from cp0 `p(t._1)`. If it is writing to `p`, will use the writing data.
    * @param p the source CP0Struct.
    * @param t the interval.
    * @return
    */
  def wtf(p: CP0Struct, t: Int): UInt = {
    val c = cp0Map(p)
    Mux(compareWriteCP0(p), wdata(t), c(t))
  }

  val excCode =
    MuxCase(
      cause(6, 2),
      Array(
        io.except(EXCEPT_INTR)         -> 0.U(5.W), // int
        io.except(EXCEPT_FETCH)        -> "h04".U(5.W), // AdEL
        io.except(EXCEPT_INST_INVALID) -> "h0a".U(5.W), // RI
        io.except(EXCEPT_OVERFLOW)     -> "h0c".U(5.W), // Ov
        io.except(EXCEPT_SYSCALL)      -> "h08".U(5.W), // Sys
        io.except(EXCEPT_BREAK)        -> "h09".U(5.W), // Break
        io.except(EXCEPT_LOAD)         -> "h04".U(5.W), // AdEL
        io.except(EXCEPT_STORE)        -> "h05".U(5.W) // AdES
      )
    )

  // @formatter:off
  // TODO add hardware write index entryLo0/1 pageMask entryHi
  index := Cat(
    /* 31     P */ 0.U,
    /* 30:n   0 */ 0.U((31-log2Ceil(tlbEntries)).W),
    /* n-1:0  index */ wtf(con_Index, (log2Ceil(tlbEntries)-1, 0))
  )

  //noinspection DuplicatedCode
  entryLo0 := Cat(
    /* 31:30  Fill*/ 0.U(2.W),
    /* 29:6   PFN */ wtf(con_EntryLo0, (29, 6)),
    /* 5:3    C   */ wtf(con_EntryLo0, (5, 3)),
    /* 2      D   */ wtf(con_EntryLo0, 2),
    /* 1      V   */ wtf(con_EntryLo0, 1),
    /* 0      G   */ wtf(con_EntryLo0, 0)
  )
  //noinspection DuplicatedCode
  entryLo1 := Cat(
    /* 31:30  Fill*/ 0.U(2.W),
    /* 29:6   PFN */ wtf(con_EntryLo1, (29, 6)),
    /* 5:3    C   */ wtf(con_EntryLo1, (5, 3)),
    /* 2      D   */ wtf(con_EntryLo1, 2),
    /* 1      V   */ wtf(con_EntryLo1, 1),
    /* 0      G   */ wtf(con_EntryLo1, 0)
  )

  pageMask := Cat(
    /* 31:29  0   */  0.U(3.W),
    /* 28:13  Mask*/  wtf(con_PageMask, (28,13)),
    /* 12:0   0   */  0.U(13.W)
  )

  badVAddr := Mux(
    io.except(EXCEPT_FETCH) ||
      io.except(EXCEPT_LOAD) ||
      io.except(EXCEPT_STORE),
    io.badAddr,
    badVAddr
  )

  /** increase 1 every two cycle */
  count := Mux(compareWriteCP0(con_Count), wdata, count + tick)

  entryHi := Cat(
    /* 31:13  VPN2  */ wtf(con_EntryHi, (31,13)),
    /* 12:11  VPN2X */ wtf(con_EntryHi, (12,11)),
    /* 10:8   0     */ 0.U(3.W),
    /* 6:0    ASID  */ wtf(con_EntryHi, (7, 0))
  )

  status := Cat(
    /* 31:23  0   */  0.U(9.W),
    /* 22     Bev */  1.U(1.W),
    /* 21:16  0   */  0.U(6.W),
    /* 15:8   IM7...IM0 */  wtf(con_Status, (15, 8)),
    /* 7:2    0   */  0.U(6.W),
    /* 1      EXL */  Mux(except, 1.U(1.W), wtf(con_Status, 1)),
    /* 0      IE  */  wtf(con_Status, 0),
  )

  cause := Cat(
    /* 31     BD  */  Mux(except, io.inDelaySlot, cause(31)),
    /* 30     TI  */  0.U(1.W),
    /* 29:16  0   */  0.U(14.W),
    /* 15:10  IP7...IP2 */ io.intr,
    /* 9:8    IP1...IP0 */ wtf(con_Cause, (9,8)),
    /* 7      0   */  0.U(1.W),
    /* 6:2    ExcCode   */ excCode,
    /* 1:0    0   */  0.U(2.W),
  )

  epc := Mux(
    except,
    Mux(io.inDelaySlot, io.pc - 4.U, io.pc),
    wtf(con_EPC, (31, 0))
  )
  // @formatter:on

  io.data := MuxLookup(
    io.addr,
    0.U,
    cp0Map.toSeq.map(e => e._1.addr.U -> e._2)
  )

  io.status_o := status
  io.cause_o  := cause
  io.EPC_o    := epc
}
