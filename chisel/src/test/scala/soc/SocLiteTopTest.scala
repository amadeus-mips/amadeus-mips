package soc

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

import scala.io.Source

//TODO: implicit parameter passing
class SocLiteTopTest extends ChiselFlatSpec {
  behavior.of("func_test")
  val funcFile = "./src/test/resources/loongson/func/inst_ram.coe"

  it should "use no backend" in {
    Driver.execute(
      Array(),
      () => new SocLiteTop(simulation = true, memFile = funcFile)
    ) { c =>
      new SocLiteTopUnitTester(c)
    } should be(true)
  }

  it should "use verilator without vcd file with performance metrics enabled" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop(simulation = true, memFile = funcFile, performanceMonitorEnable = true)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = true, performanceMonitorEnable = true)
    } should be(true)
  }

  it should "use verilator to generate vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop(simulation = true, memFile = funcFile)
    ) { c =>
      new SocLiteTopUnitTester(c, trace = true)
    } should be(true)
  }

  behavior.of("perf_test")
  val perfFile = "./src/test/resources/loongson/perf/axi_ram.coe"

  it should "use verilator without vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator", "--generate-vcd-output", "off"),
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, banLog = true, perfNumber = 4)
    } should be(true)
  }
  it should "use verilator to generate vcd file" in {
    Driver.execute(
      Array("--backend-name", "verilator"),
      () => new SocLiteTop(simulation = false, memFile = perfFile)
    ) { c =>
      new SocLiteTopUnitTester(c, banLog = true, perfNumber = 4)
    } should be(true)
  }

}

/**
  *
  * @param c  The Soc
  * @param banLog Will disable the log info, but the error message will still be output
  * @param trace Will compare to trace, only valid for func test
  * @param needAssert For auto test, if needAsert is true, the test will break when cpu goes wrong
  * @param perfNumber For perf test, it choose which test case to run. For func test, it should be 0
  */
class SocLiteTopUnitTester(
  c:                        SocLiteTop,
  banLog:                   Boolean = false,
  trace:                    Boolean = false,
  needAssert:               Boolean = false,
  perfNumber:               Int = 0,
  performanceMonitorEnable: Boolean = false
) extends PeekPokeTester(c) {
  require(perfNumber >= 0 && perfNumber <= 10)

  import chisel3._

  val trace_file = "./src/test/resources/loongson/func/golden_trace.txt"
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

  def switchData = if (perfNumber == 0) BigInt("ff", 16).toInt else 15 - perfNumber

  step(1)
  poke(c.io.gp.switch, switchData.U(8.W))
  poke(c.io.gp.btn_key_row, 0.U(4.W))
  poke(c.io.gp.btn_step, 3.U(2.W))
  reset(3)
  var iCount = 0
  var cCount = 1 // avoid divide 0
  val result = run()
  if (needAssert) require(result)
  step(10)
  info("Finished!")
  info(s"run $cCount cycles, $iCount instructions")
  info(s"IPC is ${iCount.toFloat / cCount}")
  if (performanceMonitorEnable) {
    info(
      s"there are ${peek(c.io.performance.get.cpu.cache.hitCycles)} cycles of hit, and ${peek(c.io.performance.get.cpu.cache.missCycles)} of misses"
    )
  }

  def run(): Boolean = {
    while (pc != pcEnd) {
      val current = System.currentTimeMillis()
      if (pc != 0) {
        if (pc < BigInt("9fc00000", 16) || (perfNumber != 0 && pc == BigInt("bfc00384", 16))) {
          err(lastDebugInfo)
          return false
        }
        iCount = iCount + 1
        lastDebugInfo = debugInfo
        if (current - before > 5000) {
          info(s"running: $lastDebugInfo")
          before = current
        }
        if (trace) {
          if (wen != 0 && wnum != 0) {
            if (trace_line(0) != 0) {
              if (!(pc == trace_line(1) && wnum == trace_line(2) && wdata == trace_line(3))) {
                err(lastDebugInfo)
                err(s"Should be ${trace_line.foldLeft("")(_ + " " + _.toString(16))}")
                return false
              }
            }
            if (lines.hasNext) trace_line = lines.next().split(" ").map(BigInt(_, 16))
            else return true
          }
        }
      }
      if (!(current - before < 20000)) {
        err(lastDebugInfo)
        return false
      }
      update(1)
    }
    true
  }
  def update(n: Int): Unit = {
    cCount = cCount + 1
    pc = peek(c.io.debug.wbPC)
    wen = peek(c.io.debug.wbRegFileWEn)
    wnum = peek(c.io.debug.wbRegFileWNum)
    wdata = peek(c.io.debug.wbRegFileWData)
    uartSimu()
    step(n)
  }
  def uartSimu(): Unit = {
    if (peek(c.io.uart.valid) != 0) {
      print(peek(c.io.uart.bits).toChar)
    }
  }

  def debugInfo = f"pc--0x$pc%08x, wen--${(wen != 0).toString}%5s, wnum--$wnum%02x, wdata--0x$wdata%08x"

  def err(msg: String) = {
    println(Console.RED + "Error: " + msg + Console.RESET)
  }

  def info(msg: String) = {
    if (!banLog) {
      println(Console.CYAN + "Info: " + msg + Console.RESET)
    }
  }

}
