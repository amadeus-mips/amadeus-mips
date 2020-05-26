package soc

import java.io.PrintWriter
import java.nio.file.{Files, Paths}

import chisel3.iotesters.PeekPokeTester

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import java.io._

/**
  *
  * @param c  The Soc
  * @param perfNumber For perf test, it choose which test case to run. For func test, it should be 0
  */
class SocLiteTopUnitTester(
  c:              SocLiteTop,
  perfNumber:     Int = 0
)(implicit tcfg: TestConfig)
    extends PeekPokeTester(c) {

  tcfg.check(perfNumber)

  val writeTraceFile =
    if (tcfg.writeTrace) Some(s"./src/test/resources/loongson/perf/${tcfg.perfMap(perfNumber)}/cmp.txt") else None

  val perfLog = ArrayBuffer('p','e','r','f','l','o','g','\n')

  import chisel3._


  val isPerf = tcfg.runAllPerf || perfNumber != 0

  val traceFile =
    if (isPerf && perfNumber!=0) s"./src/test/resources/loongson/perf/${tcfg.perfMap(perfNumber)}/golden_trace.txt"
    else "./src/test/resources/loongson/func/golden_trace.txt"

  /** get switch data */
  def switchData(n: Int = perfNumber) = if (n == 0) BigInt("ff", 16).toInt else 15 - n

  /** get trace from trace file */
  val source = if (tcfg.trace) Some(Source.fromFile(traceFile)) else None
  val lines = if (tcfg.trace) Some(source.get.getLines()) else None

  /** get trace target file */
  val traceWriter = if (tcfg.writeTrace) {
    val path = Paths.get(writeTraceFile.get)
    Files.createDirectories(path.getParent)
    if (!path.toFile.exists())
      Files.createFile(path)
    Some(new PrintWriter(path.toFile))
  } else None

  // init
  var trace_line = if (tcfg.trace) Some(lines.get.next().split(" ").map(BigInt(_, 16))) else None

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
  if (tcfg.runAllPerf) {
    for (i <- 1 to 10) {
      info(s"${tcfg.perfMap(i)} started:")
      resetConfreg(i)
      reInit()
      result &= run()
      info(s"${tcfg.perfMap(i)} finished.")
    }
  } else {
    resetConfreg()
    reInit()
    result &= run()
  }
  if (tcfg.needAssert) require(result)
  step(5)
  info("Finished!")
  info(s"run $cCount cycles, $iCount instructions")
  info(s"IPC is ${iCount.toFloat / cCount}")
  if (tcfg.performanceMonitorEnable) {
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
          err("Exit-wrong pc")
          return false
        }
        iCount = iCount + 1
        lastDebugInfo = debugInfo
        if (current - lastTime > 5000) {
          log(s"running: $lastDebugInfo")
          lastTime = current
        }
        if (tcfg.trace) {
          if (!traceCompare()) return false
        }
        if (tcfg.writeTrace) {
          writeTrace()
        }
      }
      if (!(current - lastTime < 20000)) {
        err(lastDebugInfo)
        err("Exit-time exceed")
        return false
      }
      update(1)
    }
    if (isPerf) {
      printPerfLog()
    }
    true
  }

  /**
    * Only for perf test, make sure the pc is not in the segment of "print"
    */
  def writeTrace(): Unit = {
    if (wen != 0 && wnum != 0 && pc < BigInt("9fc126e0", 16)) {
      traceWriter.get.println(f"1 $pc%08x $wnum%02x $wdata%08x")
    }
  }

  def traceCompare(): Boolean = {
    if (wen != 0 && wnum != 0 && !(pc >= BigInt("9fc126e0", 16) && isPerf)) {
      if (trace_line.get(0) != 0) {
        if (!(pc == trace_line.get(1) && wnum == trace_line.get(2) && wdata == trace_line.get(3))) {
          err(lastDebugInfo)
          err(s"Should be ${trace_line.get.foldLeft("")(_ + " " + _.toString(16))}")
          return false
        }
      }
      if (lines.get.hasNext) trace_line = Some(lines.get.next().split(" ").map(BigInt(_, 16)))
      else return true
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
      if (isPerf) {
        perfLog += peek(c.io.uart.bits).toChar
      }
    }
  }

  def printPerfLog(): Unit = {
    require(isPerf)
    val fileName = "./perfLog/perfLog.txt"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)))
    for (character <- perfLog) {
      writer.write(character)
    }
    writer.close()
  }

  def debugInfo = f"pc--0x$pc%08x, wen--${(wen != 0).toString}%5s, wnum--$wnum%02x, wdata--0x$wdata%08x"

  def err(msg: String) = {
    println(scala.Console.RED + "Error: " + msg + scala.Console.RESET)
  }

  def log(msg: String) = {
    if (!tcfg.banLog) {
      println(scala.Console.CYAN + "Log: " + msg + scala.Console.RESET)
    }
  }

  def info(msg: String) = {
    println(scala.Console.CYAN + "Info: " + msg + scala.Console.RESET)
  }

  if (tcfg.trace) source.get.close()
  if (tcfg.writeTrace) traceWriter.get.close()
}
