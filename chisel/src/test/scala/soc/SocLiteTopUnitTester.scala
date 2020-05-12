package soc

import chisel3.iotesters.PeekPokeTester

import scala.io.Source

/**
  *
  * @param c  The Soc
  * @param banLog Will disable the log info, but the error message will still be output
  * @param trace Will compare to trace, only valid for func test
  * @param needAssert For auto test, if needAssert is true, the test will break when cpu goes wrong
  * @param perfNumber For perf test, it choose which test case to run. For func test, it should be 0
  */
class SocLiteTopUnitTester(
  c:                        SocLiteTop,
  banLog:                   Boolean = false,
  trace:                    Boolean = false,
  needAssert:               Boolean = false,
  perfNumber:               Int = 0,
  runAllPerf:               Boolean = false,
  performanceMonitorEnable: Boolean = false
)(implicit trace_file:      String = "./src/test/resources/loongson/func/golden_trace.txt", vcdOn: Boolean = false)
    extends PeekPokeTester(c) {
  require(perfNumber >= 0 && perfNumber <= 10)
  require(!(runAllPerf && vcdOn))

  val perfMap = Map(
    (1, "bit count"),
    (2, "bubble sort"),
    (3, "coremark"),
    (4, "crc32"),
    (5, "dhrystone"),
    (6, "quick sort"),
    (7, "select sort"),
    (8, "sha"),
    (9, "stream copy"),
    (10, "string search")
  )

  import chisel3._

  val isPerf = runAllPerf || perfNumber != 0

  /** get switch data */
  def switchData(n: Int = perfNumber) = if (n == 0) BigInt("ff", 16).toInt else 15 - n

  /** get trace from trace file */
  val source = Source.fromFile(trace_file)
  val lines = source.getLines()

  // init
  var trace_line: Array[BigInt] = lines.next().split(" ").map(BigInt(_, 16))

  var lastTime:      Long = System.currentTimeMillis()
  var lastDebugInfo: String = ""

  var pc:    BigInt = peek(c.io.debug.wbPC)
  var wen:   BigInt = peek(c.io.debug.wbRegFileWEn)
  var wnum:  BigInt = peek(c.io.debug.wbRegFileWNum)
  var wdata: BigInt = peek(c.io.debug.wbRegFileWData)

  val pcEnd = BigInt("bfc00100", 16)

  // start run
  var iCount = 0
  var cCount = 1 // avoid divide 0
  var result = true
  if (runAllPerf) {
    for (i <- 1 to 10) {
      info(s"${perfMap(i)} started:")
      resetConfreg(i)
      reInit()
      result &= run()
      info(s"${perfMap(i)} finished.")
    }
  } else {
    resetConfreg()
    reInit()
    result &= run()
  }
  if (needAssert) require(result)
  step(5)
  log("Finished!")
  log(s"run $cCount cycles, $iCount instructions")
  log(s"IPC is ${iCount.toFloat / cCount}")
  if (performanceMonitorEnable) {
    log(
      s"there are ${peek(c.io.performance.get.cpu.cache.hitCycles)} cycles of hit, " +
        s"and ${peek(c.io.performance.get.cpu.cache.missCycles)} of misses, " +
        s"and ${peek(c.io.performance.get.cpu.cache.idleCycles)} of idle cycles"
    )
  }

  /**
    *
    * @param n the perf number
    */
  def resetConfreg(n: Int = perfNumber): Unit = {
    step(1)
    poke(c.io.gp.switch, switchData(n).U(8.W))
    poke(c.io.gp.btn_key_row, 0.U(4.W))
    poke(c.io.gp.btn_step, 3.U(2.W))
    reset(3)
  }

  def reInit(): Unit = {
    lastTime = System.currentTimeMillis()
    lastDebugInfo = ""

    pc = peek(c.io.debug.wbPC)
    wen = peek(c.io.debug.wbRegFileWEn)
    wnum = peek(c.io.debug.wbRegFileWNum)
    wdata = peek(c.io.debug.wbRegFileWData)
  }

  def run(): Boolean = {
    while (pc != pcEnd) {
      val current = System.currentTimeMillis()
      if (pc != 0) {
        if (pc < BigInt("9fc00000", 16) || (isPerf && pc == BigInt("bfc00384", 16))) {
          err(lastDebugInfo)
          return false
        }
        iCount = iCount + 1
        lastDebugInfo = debugInfo
        if (current - lastTime > 5000) {
          log(s"running: $lastDebugInfo")
          lastTime = current
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
      if (!(current - lastTime < 1000)) {
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

  def log(msg: String) = {
    if (!banLog) {
      println(Console.CYAN + "Log: " + msg + Console.RESET)
    }
  }

  def info(msg: String) = {
    println(Console.CYAN + "Info: " + msg + Console.RESET)
  }

  source.close()

}
