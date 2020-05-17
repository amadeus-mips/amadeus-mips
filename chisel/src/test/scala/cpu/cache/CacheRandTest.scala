package cpu.cache

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import chisel3.stage.ChiselStage
import chisel3.util._
import cpu.common.{NiseSramReadIO, NiseSramWriteIO}
import memory.memoryAXIWrap
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

class PerfectMemory(size: Int) {
  require(size % 4 == 0)
  // every element is a byte
  val memory = ArrayBuffer[Int]()
  for (i <- 0 until size) {
    memory += scala.util.Random.nextInt(256)
  }

  def dumpToDisk(fileName: String = "./perfMon/mem.txt"): Unit = {
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))
    for (i <- 0 until size by 4) {
      for (j <- i until i + 4) {
        writer.write(memory(j).toHexString.reverse.padTo(2, '0').reverse.mkString)
      }
      writer.write(s"\n")
    }
    writer.close()
  }

  def writeToMem(addr: Int, data: List[Int], writeMask: List[Boolean]): Unit = {
    require(writeMask.length == 4, "write mask should have a length of 4")
    require(addr % 4 == 0, "address should be aligned by 4")
    val addrList = Seq.tabulate(4)(addr + _)
    for (i <- 0 until 4) {
      if (writeMask(i)) {
        memory(addrList(i)) = data(i)
      }
    }
  }

  def readFromMem(addr: Int): List[Int] = {
    require(addr % 4 == 0, "address should be aligned by 4")
    val dataList = List.tabulate[Int](4)((offSet: Int) => memory(offSet + addr))
    dataList
  }

}

class PerfectTest extends FlatSpec with Matchers {
  val goldenModel = new PerfectMemory(128)
  "the perfect test" should "be able to read" in {
    for (i <- 0 until 128 by 4) {
      goldenModel.readFromMem(i) should equal(List.tabulate(4)((data: Int) => data + i))
    }
    goldenModel.dumpToDisk()
  }
  "the perfect test" should "be able to write" in {
    for (i <- 0 until 128 by 4) {
      goldenModel.writeToMem(i, List.tabulate(4)((offset: Int) => i + offset + 8), List.fill(4)(true))
    }
    for (i <- 0 until 128 by 4) {
      goldenModel.readFromMem(i) should equal(List.tabulate(4)((offset: Int) => offset + i + 8))
    }
  }
}

@chiselName
class DCacheTestModule extends Module {
  val io = IO(new Bundle {
    val rChannel = Flipped(new NiseSramReadIO)
    val wChannel = Flipped(new NiseSramWriteIO)
  })
  val cache = Module(new newDCache(8, 4, 8, false))
  val ram = Module(new memoryAXIWrap("./perfMon/mem.txt"))
  cache.io.axi <> ram.io.axi
  io.rChannel  <> cache.io.rChannel
  io.wChannel  <> cache.io.wChannel
}

@chiselName
class ICacheTestModule extends Module {
  val io = IO(new Bundle {
    val rChannel = Flipped(new NiseSramReadIO)
  })
  val cache = Module(new ICache(8, 4, 8, false))
  val ram = Module(new memoryAXIWrap("./perfMon/mem.txt"))
  cache.io.axi <> ram.io.axi
  io.rChannel  <> cache.io.rInst
}

class DCacheBaseTester(dut: DCacheTestModule, goldenModel: PerfectMemory) extends PeekPokeTester(dut) {
  def memRead(addr: Int): Unit = {
    poke(dut.io.wChannel.enable, false)
    poke(dut.io.rChannel.enable, true)
    poke(dut.io.rChannel.addr, addr)
    while (peek(dut.io.rChannel.valid) == 0) {
      step(1)
    }
    step(1)
    val ref = goldenModel.readFromMem(addr)
    var result: BigInt = BigInt(0)
    for (i <- 0 until 4) {
      result += BigInt(ref(i)) << (8 * (3-i))
    }
    expect(
      dut.io.rChannel.data,
      result,
      s"the read address is ${addr} the rchannel output is ${peek(dut.io.rChannel.data).toString(16)}, the expected result is ${result.toString(16)}"
    )
    step(1)
  }

  def memWrite(addr: Int, mask: List[Boolean] = List.fill(4)(true)): Unit = {
    val data = List.tabulate(4)((x: Int) => scala.util.Random.nextInt(256))
    poke(dut.io.wChannel.enable, true)
    poke(dut.io.rChannel.enable, false)
    poke(dut.io.rChannel.addr, addr)
    poke(dut.io.wChannel.sel, 15)
    var writeData: BigInt = BigInt(0)
    for (i <- 0 until 4) {
      writeData += BigInt(data(i)) << (8 * (3 - i))
    }
    poke(dut.io.wChannel.data, writeData)
    goldenModel.writeToMem(addr, data, mask)
    while (peek(dut.io.wChannel.valid) == 0) {
      step(1)
    }
    step(1)
  }

  def randDelay(): Unit = {
    poke(dut.io.wChannel.enable, false)
    poke(dut.io.rChannel.enable, false)
  }
}


class ICacheBaseTester(dut: ICacheTestModule, goldenModel: PerfectMemory) extends PeekPokeTester(dut) {
  def memRead(addr: Int): Unit = {
    poke(dut.io.rChannel.enable, true)
    poke(dut.io.rChannel.addr, addr)
    while (peek(dut.io.rChannel.valid) == 0) {
      step(1)
    }
    step(1)
    val ref = goldenModel.readFromMem(addr)
    var result: BigInt = BigInt(0)
    for (i <- 0 until 4) {
      result += BigInt(ref(i)) << (8 * (3-i))
    }
    expect(
      dut.io.rChannel.data,
      result,
      s"the read address is ${addr} the rchannel output is ${peek(dut.io.rChannel.data).toString(16)}, the expected result is ${result.toString(16)}"
    )
    step(1)
  }

  def randDelay(): Unit = {
    poke(dut.io.rChannel.enable, false)
  }
}

class DCacheCheckLRUTester(dut: DCacheTestModule, goldenModel: PerfectMemory) extends DCacheBaseTester(dut, goldenModel) {
  for (i <- 0 until 4) {
    memRead(i*256 + 4)
  }
  for (i <- 0 until 3) {
    memWrite(i*256 + 4)
  }
  for (i <- 4 until 11) {
    memRead(i * 256)
  }
  for (i <- 0 until 4) {
    memRead(i*256 + 4)
  }
}

class ICacheCheckLRUTester(dut: ICacheTestModule, goldenModel: PerfectMemory) extends ICacheBaseTester(dut, goldenModel) {
  memRead(4)
  memRead(256)
  memRead(512)
  memRead(768)
  memRead(1024)
}

class DCacheRandTest extends ChiselFlatSpec with Matchers {
  behavior.of("dcache testers")
  val reference = new PerfectMemory(8192)
  reference.dumpToDisk()
  it should "success" in {
    Driver.execute(
      Array("--generate-vcd-output", "on", "--backend-name", "verilator"),
      () => new DCacheTestModule
    ) { dut =>
      new DCacheBaseTester(dut, reference)
    } should be(true)
  }
  it should "check lru and evict behavior" in {
    Driver.execute(
      Array("--generate-vcd-output", "on", "--backend-name", "treadle"),
      () => new DCacheTestModule
    ) { dut =>
      new DCacheCheckLRUTester(dut, reference)
    } should be(true)
  }
}

class ICacheTest extends ChiselFlatSpec with Matchers {
  behavior.of("icache testers")
  val reference = new PerfectMemory(1 << 15)
  reference.dumpToDisk()
  it should "check the LRU bahaviors" in {
    Driver.execute(
      Array("--generate-vcd-output", "on", "--backend-name", "treadle"),
      () => new ICacheTestModule
    ) { dut =>
      new ICacheCheckLRUTester(dut, reference)
    } should be(true)
  }
}
