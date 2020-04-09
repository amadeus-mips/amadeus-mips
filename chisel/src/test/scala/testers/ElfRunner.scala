package testers

import java.io.{File, PrintWriter, RandomAccessFile}

import net.fornwall.jelf.ElfFile

import scala.collection.SortedMap

class ElfRunner {
  def elfToHex(fileName: String, outFile: String): Long = {
    val elf = ElfFile.fromFile(new File(fileName))
    val sections = Seq(".text", ".data")
    var info = SortedMap[Long, (Long, Long)]()
    for (sectionI <- 1 until elf.num_sh) {
      val section = elf.getSection(sectionI)
      if (sections.contains(section.getName)) {
        info += section.address -> (section.section_offset, section.size)
      }
    }

    // create a new file to load into memory
    val output = new PrintWriter(new File(outFile))
    val f = new RandomAccessFile(fileName, "r")
    var location = 0
    for ((address, (offset, size)) <- info) {
      while (location < address) {
        require(location + 3 < address, "assuming addresses are aligned to 4 bytes")
        output.write("00000000\n")
        location += 4
      }
      val data = new Array[Byte](size.toInt)
      f.seek(offset)
      f.read(data)
      var s = List[String]()
      for (byte <- data) {
        s = s :+ ("%02X".format(byte))
        location += 1
        if (location % 4 == 0) {
          //TODO: endianess
          output.write(s(3) + s(2) + s(1) + s(0) + "\n")
          s = List[String]()
        }
      }
    }
    output.close()
    val symbol = elf.getELFSymbol("_last")

    if (symbol != null) {
      symbol.value
    } else {
      0x400L
    }
  }
}
