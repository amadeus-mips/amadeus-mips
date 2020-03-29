package testSuite

import chisel3.iotesters.TesterOptionsManager
import cpu._
import testers.Simulate.build
import org.scalatest.{FlatSpec, Matchers}
import treadle.TreadleTester
import treadle.executable.TreadleException

class CPUFlatSpec extends FlatSpec with Matchers

//TODO: add support for loading binary in the future
//TODO: refactor this, use functions to determine the values
class CPUTestDriver(directoryName: String, memFile: String) {

  val optionsManager = new TesterOptionsManager {
    treadleOptions = treadleOptions.copy(callResetAtStartUp = true)
  }

  // set the target directory name
  optionsManager.setTargetDirName(s"./simulator_run_dir")

  // set the memory file name
  val hexName = s"${optionsManager.targetDirName}/../testMemFile/$directoryName/$memFile.txt"

  // This compiles the chisel to firrtl
  val compiledFirrtl = build(optionsManager, hexName)
  //  val endPC = elfToHex(path, hexName)
  val simulator = TreadleTester(compiledFirrtl, optionsManager)

  val endPC = 200
  var cycle = 0

  def getSeq(strPrefix: String): Seq[String] = {
    simulator.engine.validNames
      .filter(name =>
        (name.startsWith(strPrefix) && !name.endsWith("/in") && !name.contains("_T_") && !name.contains("_GEN_"))
      )
      .toList
      .sorted
  }

  def stripPrefix(symbolName: String, prefix: String): String = {
    symbolName.drop(prefix.length)
  }

  def reset(): Unit = {
    simulator.reset(5)
  }

  def printRegs(): Unit = {
    // todo: Make this prettier
    for (reg <- 0 to 31) {
      printReg(reg)
    }
  }

  def printReg(num: Int): Unit = {
    val v = simulator.peek(s"cpu.core.regFile.regs_$num")
    println(s"register ${num} of the register file has decimal value: ${v}")
  }

  def printCP0Reg(): Unit = {
    println(s"register count is ${simulator.peek(s"cpu.core.cp0.count")}")
    //    println(s"register status is ${simulator.peek(s"cpu.cpZero.regSR")}")
    println(s"register cause is ${simulator.peek(s"cpu.core.cp0.cause")}")
    println(s"register epc is ${simulator.peek(s"cpu.core.cp0.epc")}")
    println(s"register badvaddr is ${simulator.peek(s"cpu.core.cp0.badVAddr")}")
  }

  def printPC(): Unit = {
    val v = simulator.peek("cpu.core.fetch.pc")
    println(s"PC:    0x${v.toInt.toHexString}")
  }

  def printInst(addr: Int = -1): Unit = {
    val pc = if (addr < 0) simulator.peek("cpu.core.fetch.pc").toInt else addr
    // the data at the pc
    val v = simulator.peekMemory("mem.physicalMem", pc / 4)
    println(s"the instruction at 0x${pc.toHexString.padTo(3, ' ')} has hex value of 0x${v.toInt.toHexString}")
  }

  def printDMemAXISlave(): Unit = {
    println(s"Here are the signals of the data communication channel")
    val prefixString = "memAXISlave.dCon."
    val symsOfD = getSeq(prefixString)
    for (symbol <- symsOfD) {
      println(s"${stripPrefix(symbol, prefixString)} is ${simulator.peek(symbol)}")
    }
  }

  def printIMemAXISlave(): Unit = {
    println(s"Here are the signals of the instruction communication channel")
    val prefixString = "memAXISlave.iCon."
    val symsOfI = getSeq(prefixString)
    for (symbol <- symsOfI) {
      println(s"${stripPrefix(symbol, prefixString)} is ${simulator.peek(symbol)}")
    }
  }

  def printMemAXISlave(): Unit = {
    printDMemAXISlave()
    printIMemAXISlave()
  }

  def printDMemController(): Unit = {
    val prefixStringAXI = "dmem.io_axi_"
    val symsOfAXI = getSeq(prefixStringAXI)
    println(s"here are the signals that communicate with the AXI slave of the data memory")
    for (symbol <- symsOfAXI) {
      println(s"${stripPrefix(symbol, prefixStringAXI)} is ${simulator.peek(symbol)}")
    }
    val prefixStringIO = "dmem.io_bus_"
    val symsOfRam = getSeq(prefixStringIO)
    println(s"here are the signals that communicate with the data ram")
    for (symbol <- symsOfRam) {
      println(s"${stripPrefix(symbol, prefixStringIO)} is ${simulator.peek(symbol)}")
    }
  }

  def printIMemController(): Unit = {
    val prefixStringAXI = "imem.io_axi_"
    val symsOfAXI = getSeq(prefixStringAXI)
    println(s"here are the signals that communicate with the AXI slave of the instruction memory")
    for (symbol <- symsOfAXI) {
      println(s"${stripPrefix(symbol, prefixStringAXI)} is ${simulator.peek(symbol)}")
    }
    val prefixStringRam = "imem.io_bus_"
    val symsOfRam = getSeq(prefixStringRam)
    println(s"here are the signals that communicate with the ram")
    for (symbol <- symsOfRam) {
      println(s"${stripPrefix(symbol, prefixStringRam)} is ${simulator.peek(symbol)}")
    }
  }

  def printMemController(): Unit = {
    printDMemController()
    printIMemController()
  }

  def printICacheCPUIO(): Unit = {
    val namePrefix = "cpu.iCache.iCache.io_"
    val syms = getSeq(namePrefix)
    println("here are the IO between CPU and iCache")
    for (symbol <- syms) {
      println(s"${stripPrefix(symbol, namePrefix)} is ${simulator.peek(symbol)}")
    }
  }

  def printICacheAXIIO(): Unit = {
    val namePrefix = "cpu.iCache.io_axi"
    val syms = getSeq(namePrefix)
    println("here are the IO between icache and AXI")
    for (symbol <- syms) {
      println(s"${stripPrefix(symbol, namePrefix)} is ${simulator.peek(symbol)}")
    }
  }

  //TODO: strip all strings of prefix
  def printCPUAXIMaster(): Unit = {
    val namePrefix = "cpu.io_bus_axi_"
    val syms = simulator.engine.validNames.filter(name => name.startsWith(namePrefix))
    println("this is the signal of the AXI master connected to CPU")
    for (symbol <- syms) {
      println(s"${stripPrefix(symbol, namePrefix)} is ${simulator.peek(symbol)}")
    }
  }

  //TODO: module dump function
  //  def dumpAllModules(): Unit = {
  //    val modules = conf.cpuType match {
  //      case "single-cycle" => SingleCycleCPUInfo.getModules()
  //      case "pipelined"    => PipelinedCPUInfo.getModules()
  //      case other => {
  //        println(s"Cannot dump info for CPU type ${other}")
  //        List()
  //      }
  //    }
  //    for (name <- modules) {
  //      for ((symbol, name) <- getIO(name)) {
  //        val v = simulator.peek(symbol)
  //        println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
  //      }
  //    }
  //  }

  //  def listModules(): Unit = {
  //    val modules = conf.cpuType match {
  //      case "single-cycle" => SingleCycleCPUInfo.getModules()
  //      case "pipelined"    => PipelinedCPUInfo.getModules()
  //      case other => {
  //        println(s"Cannot dump info for CPU type ${other}")
  //        List()
  //      }
  //    }
  //    println("Available modules to dump I/O")
  //    println("-----------------------------")
  //    for (name <- modules) {
  //      println(s"${name}")
  //    }
  //  }

  //  def dumpModule(module: String): Unit = {
  //    for ((symbol, name) <- getIO(module)) {
  //      val v = simulator.peek(symbol)
  //      println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
  //    }
  //  }

  def getIO(module: String): Map[String, String] = {
    module match {
      case "dmem" => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"cpu.io_dmem_"))
        syms.map { sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.') } toMap
      }
      case "imem" => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"cpu.io_imem_"))
        syms.map { sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.') } toMap
      }
      case "dmemPort" => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"mem.io_dmem_"))
        syms.map { sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.') } toMap
      }
      case "imemPort" => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"mem.io_imem_"))
        syms.map { sym => sym -> sym.substring(sym.indexOf('_') + 1).replace('_', '.') } toMap
      }
      case other => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"cpu.${other}.io_"))
        syms.map { sym => sym -> s"${other}.io.${sym.substring(sym.indexOf('_') + 1)}" } toMap
      }
    }
  }

  def printAllPipeRegs(): Unit = {
    val pipeLineStages = "if_id" :: "id_exe" :: "exe_mem" :: "mem_wb" :: Nil
    for (reg <- pipeLineStages) {
      printPipeReg(reg)
    }
  }

  def printPipeReg(module: String): Unit = {
    println(s"these are the values for $module")
    println("--------------------------------")
    val modulePrefix = s"cpu.core.$module.pipeReg_"
    val symsRegValue = getSeq(modulePrefix)
    println(s"register $module hold values of")
    for (sym <- symsRegValue) {
      val value = simulator.peek(sym)
      println(
        s"${stripPrefix(sym, modulePrefix)} has decimal value: ${value} hex value: (0x${value.toInt.toHexString})"
      )
    }
    val ioPrefix = s"cpu.core.$module.io_"
    val symsIO = getSeq(ioPrefix)
    println(s"these are the io values")
    for (sym <- symsIO) {
      val value = simulator.peek(sym)
      println(
        s"${stripPrefix(sym, ioPrefix)} has decimal value: $value and hex value: (0x${value.toInt.toHexString})"
      )
    }
    println("-------------------------------")
  }

  def checkRegs(vals: Map[Int, BigInt]): Boolean = {
    var success = true
    for ((num, value) <- vals) {
      try {
        simulator.expect(s"cpu.core.regFile.regs_$num", value)
      } catch {
        case _: TreadleException => {
          success = false
          val real = simulator.peek(s"cpu.core.regFile.regs_$num")
          println(s"Register $num failed to match. Was $real. Should be $value")
        }
      }
    }
    success
  }

  def checkMemory(vals: Map[Int, BigInt]): Boolean = {
    var success = true
    for ((addr, value) <- vals) {
      try {
        simulator.expectMemory("mem.physicalMem", addr, value)
      } catch {
        case e: TreadleException => {
          success = false
          val real = simulator.peekMemory("mem.physicalMem", addr)
          println(s"Memory at address 0x${addr.toHexString} failed to match. Was $real. Should be $value")
        }
      }
    }
    success
  }

  def step(cycles: Int = 0): Unit = {
    val start = cycle
    while (simulator.peek("cpu.core.fetch.pc") != endPC && cycle < start + cycles) {
      simulator.step(1)
      cycle += 1
    }
    println(s"Current cycle: ${cycle}")
  }

  def run(cycles: Int): Unit = {
    while (cycle < cycles) {
      simulator.step(1)
      cycle += 1
    }
  }

  def dumpSymbolNames(): Unit = {
    simulator.engine.symbolTable.nameToSymbol.keys.toSeq.sorted.foreach { key => println(s"symbol: $key") }
  }
}

case class CPUTestCase(
  directoryName: String,
  memFile:       String,
  cycles:        Int,
  checkRegs:     Map[Int, BigInt],
  checkMem:      Map[Int, BigInt]
) {
  def name(): String = {
    directoryName + "/" + memFile
  }
}

// the companion object
object CPUTestDriver {
  def apply(testCase: CPUTestCase): Boolean = {
    val driver = new CPUTestDriver(testCase.directoryName, testCase.memFile)
    driver.run(testCase.cycles)
    val success = driver.checkRegs(testCase.checkRegs)
    success && driver.checkMemory(testCase.checkMem)
  }
}
