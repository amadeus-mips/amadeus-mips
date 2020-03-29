package cpu

import java.io.{File, PrintWriter, RandomAccessFile}

import chisel3.iotesters.TesterOptionsManager
import chisel3.{ChiselExecutionSuccess, HasChiselExecutionOptions}
import firrtl.{ExecutionOptionsManager, HasFirrtlOptions}
import net.fornwall.jelf.ElfFile
import treadle.{HasTreadleOptions, TreadleOptionsManager}

import scala.collection.SortedMap

/**
 * Simple object with only a main function to run the treadle simulation.
 * When run, this will begin execution and continue until the PC reaches the
 * "_last" symbol in the elf file or the max cycle parameter is reached
 * {{{
 *  sbt> runMain dinocpu.simulate [options] <riscv binary> <CPU type>
 * }}}
 */
object Simulate {
  //  val helptext = "usage: simulate <riscv binary> <CPU type>"
  val helptext = "usage: simulate <CPU type>"

  // parse the elf file and output the hex file
  def elfToHex(filename: String, outfile: String) = {
    val elf = ElfFile.fromFile(new java.io.File(filename))
    val sections = Seq(".text", ".data") // These are the sections we want to pull out
    // address to put the data -> offset into the binary, size of section
    var info = SortedMap[Long, (Long, Long)]()
    // Search for these section names
    for (i <- 1 until elf.num_sh) {
      val section = elf.getSection(i)
      if (sections.contains(section.getName)) {
        //println("Found "+section.address + " " + section.section_offset + " " + section.size)
        info += section.address -> (section.section_offset, section.size)
      }
    }

    // Now, we want to create a new file to load into our memory
    val output = new PrintWriter(new File(outfile))
    val f = new RandomAccessFile(filename, "r")
    // println("Length: "+ f.length)
    var location = 0
    for ((address, (offset, size)) <- info) {
      //println(s"Skipping until $address")
      while (location < address) {
        require(location + 3 < address, "Assuming addresses aligned to 4 bytes")
        output.write("00000000\n")
        location += 4
      }
      //println(s"Writing $size bytes")
      val data = new Array[Byte](size.toInt)
      f.seek(offset)
      f.read(data)
      var s = List[String]()
      for (byte <- data) {
        s = s :+ ("%02X".format(byte))
        location += 1
        if (location % 4 == 0) {
          // Once we've read 4 bytes, swap endianness
          output.write(s(3) + s(2) + s(1) + s(0) + "\n")
          s = List[String]()
        }
      }
      //println(s"Wrote until $location")
    }
    output.close()
    // Return the final PC value we're looking for
    val symbol = elf.getELFSymbol("_last")

    if (symbol != null) symbol.value
    else 0x400L
  }

  def build(optionsManager: TesterOptionsManager, memFileName: String): String = {
    optionsManager.firrtlOptions = optionsManager.firrtlOptions.copy(compilerName = "low")

    chisel3.Driver.execute(optionsManager, () => new TestTop(memFileName)) match {
      case ChiselExecutionSuccess(Some(_), _, Some(firrtlExecutionResult)) =>
        firrtlExecutionResult match {
          case firrtl.FirrtlExecutionSuccess(_, compiledFirrtl) =>
            compiledFirrtl
          case firrtl.FirrtlExecutionFailure(message) =>
            throw new Exception(s"FirrtlBackend: Compile failed. Message: $message")
        }
      case _ =>
        throw new Exception("Problem with compilation")
    }
  }

  //  def main(args: Array[String]): Unit = {
  //    require(args.length >= 3, "Error: Expected at least three argument\n" + helptext)
  //    //   don't use the binary, manually load the mem file
  ////    require(args.length >= 1, "Error: Expected at least 1 argument\n" + helptext)
  //    val optionsManager = new SimulatorOptionsManager
  //    val cpuType = args(0)
  //    val directoryName = args(1)
  //    val memFile = args(2)
  //
  //    optionsManager.setTargetDirName("./simulator_run_dir")
  //
  //    val hexName = s"${optionsManager.targetDirName}/../testMemFile/$directoryName/$memFile"
  //
  //    // Create the CPU config. This sets the type of CPU and the binary to load
  //    val conf = new CPUConfig()
  //    conf.cpuType = cpuType
  //    conf.memFile = hexName
  //
  //    // This compiles the chisel to firrtl
  //    val compiledFirrtl = build(optionsManager, conf)
  //
  //    // this is working as expected in hex format
  //    val endPC = 0x00000040
  //    // Instantiate the simulator
  //    val simulator = TreadleTester(compiledFirrtl, optionsManager)
  //
  //    // Make sure the system is in the reset state (5 cycles)
  //    // for some reason, settings it back to 1 restores the initial PC to 0
  ////    simulator.reset(5)
  //
  //    // This is the actual simulation
  //
  //    var cycles = 0
  //    val maxCycles =
  //      if (optionsManager.simulatorOptions.maxCycles > 0) optionsManager.simulatorOptions.maxCycles else 200
  ////    val maxCycles = 2000000
  //    // Simulate until the pc is the "endPC" or until max cycles has been reached
  //    println("Running...")
  //    // print the pc initial position
  //    //    println(s"the position of the initial PC is ${simulator.peek("cpu.regPC")}")
  //
  ////  memory peek helper
  ////     Note: this does work with written mem
  ////    for (i <- 0 until 8) {
  ////      println(s"the instruction at position $i is ${simulator.peekMemory("mem.physicalMem", i)}")
  ////    }
  //
  //    // simulate until the max cycles are reached or the pc reaches the end pc
  //    while (simulator.peek("cpu.regPC") != endPC && cycles < maxCycles) {
  //
  //      // for small simulation, print pc every cycle
  //      println(s"Simulation results for cycle $cycles")
  //      // this is becasue PC is updated at the end of every cycle
  //
  //      // print pc position
  //      //      println(s"pc position is at ${simulator.peek("cpu.regPC")}")
  //      // print loaded instruction
  ////      println(s"intruction loaded is ${simulator.peek("cpu.controller.io_input_instr")}")
  //      // print alu signals
  ////      println(s"alu controller signal is ${simulator.peek("cpu.alu.io_input_controlSignal")}")
  ////      println(s"alu output is ${simulator.peek("cpu.alu.io_output_aluOutput")}")
  ////      println(s"alu offset selection is ${simulator.peek("cpu.alu.io_input_inputB")}")
  //      // print registers
  ////      println(s"regfile write enable signal is ${simulator.peek("cpu.regFile.io_writeEnable")}")
  ////      println(s"regFile write address is ${simulator.peek("cpu.regFile.io_writeAddr")}")
  ////      println(s"regFile write data is ${simulator.peek("cpu.regFile.io_writeData")}")
  ////      // warning: the register values will be updated on the next cycle
  ////      println(s"register t1 is ${simulator.peek("cpu.regFile.regs_9")}")
  //      println(s"register t2 is ${simulator.peek("cpu.regFile.regs_10")}")
  ////      println(s"register t3 is ${simulator.peek("cpu.regFile.regs_11")}")
  ////      println(s"response from actual mem signal is ${simulator.peek("dmem.io_bus_response_bits_data")}")
  ////      println(s"valid signal response from actual mem is ${simulator.peek("dmem.io_bus_response_valid")}")
  ////      println(s"operation signal is ${simulator.peek("dmem.io_bus_request_bits_operation")}")
  ////      println(s"memory mask is ${simulator.peek("dmem.io_pipeline_maskmode")}")
  ////      println(s"data passed to it is ${simulator.peek("dmem.io_pipeline_writedata")}")
  ////      println(s"sext is ${simulator.peek("dmem.io_pipeline_sext")}")
  ////      println(s"memory write data is ${simulator.peek("dmem.io_pipeline_mask")}")
  ////      println(s"the memory mask signal is ${simulator.peek("cpu.controller.io_output_MemMask")}")
  ////      println(s"the memory sign extend is ${simulator.peek("cpu.controller.io_output_MemSext")}")
  //
  //      // advance the simulator
  //      simulator.step(1)
  //      cycles += 1
  //
  //    }
  ////    println(s"actual data written is ${simulator.peekMemory("mem.physicalMem",24)}")
  //    println(s"TOTAL CYCLES: $cycles")
  //
  //    // manually verify for now
  //    // Note: verification should not be in the process of simulation, as this
  //    // reg files will not "poke" correctly on the same cycle
  //    //TODO: bridge an interface between simulation results and results from an actual simulator
  //    println(s"Register t2: ${simulator.peek("cpu.regFile.regs_10")}")
  //    if (!(simulator.peek("cpu.regFile.regs_10") == 20)) {
  //      println("VERIFICATION FAILED")
  //    } else {
  //      println("VERIFICATION SUCCEEDED")
  //    }
  //  }
}

case class SimulatorOptions(maxCycles: Int = 0) extends firrtl.ComposableOptions {}

trait HasSimulatorOptions {
  self: ExecutionOptionsManager =>

  val simulatorOptions = SimulatorOptions()

  parser.note("simulator-options")

  parser
    .opt[Int]("max-cycles")
    .abbr("mx")
    .valueName("<long-value>")
    .foreach { x => simulatorOptions.copy(maxCycles = x) }
    .text("Max number of cycles to simulate. Default is 0, to continue simulating")
}

class SimulatorOptionsManager extends HasSimulatorSuite

trait HasSimulatorSuite
  extends TreadleOptionsManager
    with HasChiselExecutionOptions
    with HasFirrtlOptions
    with HasTreadleOptions
    with HasSimulatorOptions {
  self: ExecutionOptionsManager =>
}
