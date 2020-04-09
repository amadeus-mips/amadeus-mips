package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import scala.io.Source

class SocLiteTopTest extends ChiselFlatSpec {
  behavior.of("Soc")

  val instFile = "./src/test/resources/loongsonFunc/inst_ram.coe"
  "pc" should "run to 0xbfc00100" in {
    Driver.execute(Array(), () => new SocLiteTop(simulation = true, memFile = instFile)) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }

  it should "satisfy golden_trace" in {
    Driver.execute(Array("--backend-name", "verilator"), () => new SocLiteTop(simulation = true, memFile = instFile)) {
      c => new SocLiteTopUnitTester(c, trace = true)
    }
  }
  it should "generate gcd file" in {
    Driver.execute(
      Array(
        "--backend-name",
        "verilator",
        "--generate-vcd-output",
        "on",
        "-td",
        "test_run_dir/soc_lite/vcd",
        "--top-name",
        "soc"
      ),
      () => new SocLiteTop(simulation = true, memFile = instFile)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = false)
    } should be(true)
  }

}

class SocLiteTopUnitTester(c: SocLiteTop, banLog: Boolean = false, trace: Boolean = false) extends PeekPokeTester(c) {

  import chisel3._

  val trace_file = "./src/test/resources/loongsonFunc/golden_trace.txt"
  val source = Source.fromFile(trace_file)
  val lines = source.getLines()
  var trace_line = lines.next().split(" ").map(BigInt(_, 16))

  var before = System.currentTimeMillis()
  val pcEnd = BigInt("bfc00100", 16)

  var pc:    BigInt = peek(c.io.debug.wbPC)
  var wen:   BigInt = peek(c.io.debug.wbRegFileWEn)
  var wnum:  BigInt = peek(c.io.debug.wbRegFileWNum)
  var wdata: BigInt = peek(c.io.debug.wbRegFileWData)

  var lastDebugInfo = ""

  step(1)
  poke(c.io.gp.switch, "hff".U(8.W))
  poke(c.io.gp.btn_key_row, 0.U(4.W))
  poke(c.io.gp.btn_step, 3.U(2.W))
  reset(3)
  var break = false
  while (pc != pcEnd && !break && pc != BigInt("bfc016d8", 16)) {
    val current = System.currentTimeMillis()
    if (pc != 0) {
      lastDebugInfo = debugInfo
      if (current - before > 5000) {
        printDebug(lastDebugInfo)
        before = current
      }
      if (trace) {
        if (wen != 0 && wnum != 0) {
          if (trace_line(0) != 0) {
            assert(pc == trace_line(1) && wnum == trace_line(2) && wdata == trace_line(3), lastDebugInfo)
          }
          if (lines.hasNext) trace_line = lines.next().split(" ").map(BigInt(_, 16))
          else break = true
        }
      }
    }
    assert(current - before < 60000, lastDebugInfo) // if pc keep 0 in 60s
    update(1)
  }
  step(10)

  def update(n: Int): Unit = {
    pc = peek(c.io.debug.wbPC)
    wen = peek(c.io.debug.wbRegFileWEn)
    wnum = peek(c.io.debug.wbRegFileWNum)
    wdata = peek(c.io.debug.wbRegFileWData)
    super.step(n)
  }
  def debugInfo = f"pc--0x$pc%08x, wen--${(wen != 0).toString}%5s, wnum--$wnum%02x, wdata--0x$wdata%08x"

  def printDebug(msg: String) = {
    if (!banLog) {
      println("Debug: " + msg)
    }
  }

}
