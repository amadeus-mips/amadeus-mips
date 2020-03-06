package cpu.testing

import chisel3.iotesters.TesterOptionsManager
import cpu._
import cpu.pipelined._
import cpu.simulate.build
import cpu.singleCycle.SingleCycleCPUInfo
import firrtl.stage.FirrtlSourceAnnotation
import org.scalatest.{FlatSpec, Matchers}
import treadle.TreadleTester
import treadle.executable.TreadleException

class CPUFlatSpec extends FlatSpec with Matchers

//TODO: add support for loading binary in the future
class CPUTestDriver(cpuType: String, directoryName: String, memFile: String) {

  val optionsManager = new TesterOptionsManager {
    treadleOptions = treadleOptions.copy(callResetAtStartUp = true)
  }

  // set the target directory name
  optionsManager.setTargetDirName(s"./simulator_run_dir")

  // set the memory file name
  val hexName = s"${optionsManager.targetDirName}/../testMemFile/$directoryName/$memFile.txt"

  val conf = new CPUConfig()
  conf.cpuType = cpuType
  conf.memFile = hexName

  // Convert the binary to a hex file that can be loaded by treadle
  // (Do this after compiling the firrtl so the directory is created)
//  val path = if (binary.endsWith(".riscv")) {
//    s"src/test/resources/c/${binary}"
//  } else {
//    s"src/test/resources/risc-v/${binary}"

//  }

  // This compiles the chisel to firrtl
  val compiledFirrtl = build(optionsManager, conf)
  //  val endPC = elfToHex(path, hexName)
  val endPC = 200

// Instantiate the simulator
  val sourceAnnotation = FirrtlSourceAnnotation(compiledFirrtl)

  val simulator = TreadleTester(compiledFirrtl, optionsManager)

  //  val simulator = TreadleTester(sourceAnnotation +: optionsManager.toAnnotationSeq)
  var cycle = 0

  def reset(): Unit = {
    simulator.reset(5)
  }

  def initRegs(vals: Map[Int, BigInt]) {
    for ((num, value) <- vals) {
      simulator.poke(s"cpu.regFile.regs_$num", value)
    }
  }

  def printRegs(): Unit = {
    // todo: Make this prettier
    for (reg <- 0 to 31) {
      printReg(reg)
    }
  }

  def printReg(num: Int): Unit = {
    val v = simulator.peek(s"cpu.regFile.regs_$num")
    println(s"register ${num}: ${v}")
  }

  def printCP0Reg(): Unit = {
    println(s"register count is ${simulator.peek(s"cpu.cpZero.regCount")}")
    println(s"register status is ${simulator.peek(s"cpu.cpZero.regSR")}")
    println(s"register cause is ${simulator.peek(s"cpu.cpZero.regCause")}")
    println(s"register epc is ${simulator.peek(s"cpu.cpZero.regEPC")}")
    println(s"register ebase is ${simulator.peek(s"cpu.cpZero.regEBase")}")
  }

  def printPC(): Unit = {
    val v = simulator.peek("cpu.regPC")
    println(s"PC:    0x${v.toInt.toHexString}")
  }

  def printInst(addr: Int = -1): Unit = {
    val pc = if (addr < 0) simulator.peek("cpu.regPC").toInt else addr
    // the data at the pc
    val v = simulator.peekMemory("mem.physicalMem", pc / 4)
    println(s"the instruction at 0x${pc.toHexString.padTo(3, ' ')} has hex value of 0x${v.toInt.toHexString}")
  }

  def dumpAllModules(): Unit = {
    val modules = conf.cpuType match {
      case "single-cycle" => SingleCycleCPUInfo.getModules()
      case "pipelined"    => PipelinedCPUInfo.getModules()
      case other => {
        println(s"Cannot dump info for CPU type ${other}")
        List()
      }
    }
    for (name <- modules) {
      for ((symbol, name) <- getIO(name)) {
        val v = simulator.peek(symbol)
        println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
      }
    }
  }

  def listModules(): Unit = {
    val modules = conf.cpuType match {
      case "single-cycle" => SingleCycleCPUInfo.getModules()
      case "pipelined"    => PipelinedCPUInfo.getModules()
      case other => {
        println(s"Cannot dump info for CPU type ${other}")
        List()
      }
    }
    println("Available modules to dump I/O")
    println("-----------------------------")
    for (name <- modules) {
      println(s"${name}")
    }
  }

  def dumpModule(module: String): Unit = {
    for ((symbol, name) <- getIO(module)) {
      val v = simulator.peek(symbol)
      println(s"${name.padTo(30, ' ')} ${v} (0x${v.toInt.toHexString})")
    }
  }

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
      case other => {
        val syms = simulator.engine.validNames.filter(name => name.startsWith(s"cpu.${other}.io_"))
        syms.map { sym => sym -> s"${other}.io.${sym.substring(sym.indexOf('_') + 1)}" } toMap
      }
    }
  }

  def printAllPipeRegs(): Unit = {
    for (reg <- PipelinedCPUInfo.getPipelineRegs()) {
      printPipeReg(reg)
    }
  }

  def printPipeReg(module: String): Unit = {
    val syms =
      simulator.engine.validNames.filter(name =>
        (!name.startsWith(s"cpu.$module.register")) && (!name.startsWith(s"cpu.$module._")) && (name.startsWith(
          s"cpu.${module}"
        )) && !name.endsWith("/in")
      )
    for (sym <- syms) {
      val value = simulator.peek(sym)
      println(s"${sym.padTo(30, ' ')} ${value} (0x${value.toInt.toHexString})")
    }
  }

  /**
    *
    * @param vals holds "addresses" to values. Where address is the nth *word*
    */
  def initMemory(vals: Map[Int, BigInt]): Unit = {
    for ((addr, value) <- vals) {
      simulator.pokeMemory(s"cpu.mem.physicalMem", addr, value)
    }
  }

  def checkRegs(vals: Map[Int, BigInt]): Boolean = {
    var success = true
    for ((num, value) <- vals) {
      try {
        simulator.expect(s"cpu.regFile.regs_$num", value)
      } catch {
        case _: TreadleException => {
          success = false
          val real = simulator.peek(s"cpu.regFile.regs_$num")
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
    while (simulator.peek("cpu.regPC") != endPC && cycle < start + cycles) {
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
}

case class CPUTestCase(
  directoryName: String,
  memFile:       String,
  cycles:        Map[String, Int],
  initRegs:      Map[Int, BigInt],
  checkRegs:     Map[Int, BigInt],
  initMem:       Map[Int, BigInt],
  checkMem:      Map[Int, BigInt]
) {
  def name(): String = {
    directoryName + "/" + memFile
  }
}

// the companion object
object CPUTestDriver {
  def apply(cpuType: String, testCase: CPUTestCase): Boolean = {
    val driver = new CPUTestDriver(cpuType, testCase.directoryName, testCase.memFile)
    driver.initRegs(testCase.initRegs)
    driver.initMemory(testCase.initMem)
    driver.run(testCase.cycles(cpuType))
    val success = driver.checkRegs(testCase.checkRegs)
    success && driver.checkMemory(testCase.checkMem)
  }
}
