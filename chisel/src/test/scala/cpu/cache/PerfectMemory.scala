package cpu.cache
import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

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
    require(addr % 4 == 0, "request should be aligned by 4")
    val addrList = Seq.tabulate(4)(addr + _)
    for (i <- 0 until 4) {
      if (writeMask(i)) {
        memory(addrList(i)) = data(i)
      }
    }
  }

  def readFromMem(addr: Int): List[Int] = {
    require(addr % 4 == 0, "request should be aligned by 4")
    val dataList = List.tabulate[Int](4)((offSet: Int) => memory(offSet + addr))
    dataList
  }

}