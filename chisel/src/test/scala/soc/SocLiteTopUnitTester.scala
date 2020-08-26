package soc

import java.io.{PrintWriter, _}
import java.nio.file.{Files, Paths}

import chisel3.iotesters.PeekPokeTester
import shared.GItHelper

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  *
  * @param c  The Soc
  * @param perfNumber For perf test, it choose which test case to run. For func test, it should be 0
  */
class SocLiteTopUnitTester(
  c:             SocLiteTop,
  perfNumber:    Int = 0
)(implicit tcfg: TestConfig)
    extends PeekPokeTester(c) {

  tcfg.check(perfNumber)

  val baseOutputPath = s"./out/"

  val writeTraceFile =
    if (tcfg.writeTrace) Some(s"./src/test/resources/loongson/perf/${tcfg.perfMap(perfNumber)}/cmp.txt") else None

  val perfLog = ArrayBuffer('p', 'e', 'r', 'f', 'l', 'o', 'g', '\n')
  val perfRes = new mutable.StringBuilder()
  val perfAllRes = ArrayBuffer[PerfResult]()

  import chisel3._

  val isPerf = tcfg.runAllPerf || perfNumber != 0

  val traceFile =
    if (isPerf && perfNumber != 0) s"./src/test/resources/loongson/perf/${tcfg.perfMap(perfNumber)}/golden_trace.txt"
    else "./src/test/resources/loongson/func/golden_trace.txt"

  /** get switch data */
  def switchData(n: Int = perfNumber) = if (n == 0) BigInt("ff", 16).toInt else 15 - n

  /** get trace from trace file */
  val source = if (tcfg.trace) Some(Source.fromFile(traceFile)) else None
  val lines  = if (tcfg.trace) Some(source.get.getLines()) else None

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

  var lastTime:      Long   = System.currentTimeMillis()
  var lastDebugInfo: String = ""

  var lastNum: BigInt = peek(c.io.num.data)
  var pc:      BigInt = peek(c.io.debug(0).wbPC)
  var wen:     BigInt = peek(c.io.debug(0).wbRegFileWEn)
  var wnum:    BigInt = peek(c.io.debug(0).wbRegFileWNum)
  var wdata:   BigInt = peek(c.io.debug(0).wbRegFileWData)

  val pcEnd = BigInt("bfc00100", 16)

  // start run
  var iCount   = 0
  var cCount   = 1 // avoid divide 0
  var result   = true
  var errCount = 0
  if (tcfg.runAllPerf) {
    for (i <- 1 to 10) {
      info(s"${tcfg.perfMap(i)} started:")
      resetConfreg(i)
      reInit()
      result &= run()
      info(s"${tcfg.perfMap(i)} finished.")
      if(result) afterRun()
    }
  } else {
    resetConfreg()
    reInit()
    result &= run()
    if(result)afterRun()
  }
  if (tcfg.needAssert) require(result)
  step(5)
  if(result)afterAllRun()

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
    if(isPerf){
      perfRes.clear()
    }
    lastTime      = System.currentTimeMillis()
    lastDebugInfo = ""

    lastNum  = peek(c.io.num.data)
    errCount = 0

    updateDebug(0)
  }

  def run(): Boolean = {
    while (pc != pcEnd) {
      val current = System.currentTimeMillis()
      for(i <- 0 until 2) {
        if (pc != 0) {
          if ((!tcfg.tlbTest && pc < BigInt("9fc00000", 16)) || (isPerf && pc == BigInt("bfc00384", 16))) {
            err(lastDebugInfo)
            err("Exit-wrong pc")
            return false
          }
          iCount = if(i == 0) iCount + 1 else iCount
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
        updateDebug(1)
      }
      if (!(current - lastTime < 6000)) {
        err(lastDebugInfo)
        err("Exit-time exceed")
        return false
      }
      update(1)
    }
    true
  }

  def afterRun(): Unit = {
    if (isPerf) {
      val thisResult = new PerfResult(perfRes.toString())
      perfAllRes += thisResult
      if (!thisResult.pass) {
        err(s"${thisResult.name} has failed")
        expect(good = false, s"                      ${thisResult.name} has failed")
      }
      printPerfLog()
      branchPerformanceMonitor()
    }
  }

  def afterAllRun(): Unit = {
    info("Finished!")
    info(s"run $cCount cycles, $iCount instructions")
    info(s"IPC is ${iCount.toFloat / cCount}")
    step(5)
    printPerfRes()
  }

  def printPerfRes(): Unit = {
    if(isPerf){
      val averageScore = Math.pow(perfAllRes.map(_.score).product, 1.0/perfAllRes.length)
      val commitTime = GItHelper.dateString
      val resStr = "{" + s""""commit": "$commitTime", """ + s""""score": $averageScore,"perfs":""" + "[" +  perfAllRes.map(_.toString).mkString(",") + "]}"

      val prefix = if(tcfg.runAllPerf) "all" else tcfg.perfMap(perfNumber)
      val currentTime = System.currentTimeMillis()
      val path = Paths.get(baseOutputPath + s"perf-res/$prefix$currentTime.json")
      Files.createDirectories(path.getParent)
      if(!path.toFile.exists())
        Files.createFile(path)
      val printer = new PrintWriter(path.toFile)
      printer.print(resStr)
      printer.close()
    }
  }

  def branchPerformanceMonitor(): Unit = {
    val predSuccess  = peek(c.io.branchPerf.total.success)
    val predFail     = peek(c.io.branchPerf.total.fail)
    val predJSuccess = peek(c.io.branchPerf.j.success)
    val predJFail    = peek(c.io.branchPerf.j.fail)
    val predBSuccess = peek(c.io.branchPerf.b.success)
    val predBFail    = peek(c.io.branchPerf.b.fail)

    info(branchPredictMessage(predSuccess, predFail, "total"))
    log(branchPredictMessage(predJSuccess, predJFail, "J"))
    log(branchPredictMessage(predBSuccess, predBFail, "B"))

    val res = perfAllRes.reverse.head
    res.add("predSuccess", predSuccess)
    res.add("predFail", predFail)
    res.add("predJSuccess", predJSuccess)
    res.add("predJFail", predJFail)
    res.add("predBSuccess", predBSuccess)
    res.add("predBFail", predBFail)
  }

  def branchPredictMessage(success: BigInt, fail: BigInt, description: String): String = {
    s"prediction $description: success--$success, fail--$fail" + (if(fail+success!=0) s", rate ${success.toDouble / (success+fail).toDouble}" else "")
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
    uartSimu()
    if (!isPerf) numSimu()
    step(n)
    updateDebug(0)
  }

  def updateDebug(n: Int): Unit = {
    require(n == 0 || n == 1)
    pc     = peek(c.io.debug(n).wbPC)
    wen    = peek(c.io.debug(n).wbRegFileWEn)
    wnum   = peek(c.io.debug(n).wbRegFileWNum)
    wdata  = peek(c.io.debug(n).wbRegFileWData)
  }

  def uartSimu(): Unit = {
    if (peek(c.io.uart.valid) != 0) {
      val ch = peek(c.io.uart.bits).toChar
      print(ch)
      if (isPerf) {
        perfLog += ch
        perfRes += ch
      }
    }
  }
  def numSimu(): Unit = {
    val nowNum = peek(c.io.num.data)
    if (nowNum != lastNum && peek(c.io.num.monitor) != 0) {
      // low 8 bits
      if ((nowNum & BigInt("ff", 16)) != ((lastNum & BigInt("ff", 16)) + 1)) {
        err(s"$errCount, Occurred in number ${(nowNum >> 24) & BigInt("ff", 16)} Functional Test Point!")
        errCount += 1
      } else if (((nowNum >> 24) & BigInt("ff", 16)) != (((lastNum >> 24) & BigInt("ff", 16)) + 1)) {
        err(s"$errCount, Unknown, Functional Test Point numbers are unequal!")
        errCount += 1
      } else {
        info(s"---- Number ${(nowNum >> 24) & BigInt("ff", 16)} Functional Test Point Pass!!!")
      }
    }
    lastNum = nowNum
  }

  def printPerfLog(): Unit = {
    require(isPerf)
    val fileName = "./perfLog/perfLog.txt"
    val path     = Paths.get(fileName)
    Files.createDirectories(path.getParent)
    if (!path.toFile.exists())
      Files.createFile(path)
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
