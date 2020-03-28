package memory

import chisel3._
import chisel3.util.{Cat, Fill}

/**
  * The instruction memory port.
  *
  * The I/O for this module is defined in [[IMemPortIO]].
  */
import memory.MemOperations._
class ICombinMemPort extends BaseIMemPort {
  // When the pipeline is supplying a high valid signal
  when (io.pipeline.valid) {
    val request = Wire(new Request)
    request.address   := io.pipeline.address
    request.operation := Read
    request.writedata := 0.U

    io.bus.request.bits  := request
    io.bus.request.valid := true.B
  } .otherwise {
    io.bus.request.valid := false.B
  }

  // When the memory is outputting a valid instruction
  io.pipeline.good := true.B
  io.pipeline.instruction := io.bus.response.bits.data
}

/**
  * The data memory port.
  *
  * The I/O for this module is defined in [[DMemPortIO]].
  */
class DCombinMemPort extends BaseDMemPort {
  io.pipeline.good := true.B

  when (io.pipeline.valid && (io.pipeline.memread || io.pipeline.memwrite)) {
    // Check that we are not issuing a read and write at the same time
    assert(!(io.pipeline.memread && io.pipeline.memwrite))

    io.bus.request.bits.address := io.pipeline.address
    io.bus.request.valid := true.B

    when (io.pipeline.memwrite) {
      // We issue a ReadWrite to the backing memory.
      // Basic run-down of the ReadWrite operation:
      // - DCombinMemPort sends a ReadWrite at a specific address, **addr**.
      // - Backing memory outputs the data at **addr** in io.response
      // - DCombinMemPort notes that io.memwrite is high in the response path. io.response.bits.data
      //   is masked and sign extended, and sent down io.request.writedata
      // - Backing memory receives the modified writedata and feeds it into the memory at **addr**.
      // Since this is combinational logic, this should theoretically all resolve in one clock cycle with no issues
      io.bus.request.bits.operation := ReadWrite
    } .otherwise {
      // Issue a normal read to the backing memory
      io.bus.request.bits.operation := Read
    }
  } .otherwise {
    // no request coming in so don't send a request out
    io.bus.request.valid := false.B
  }

  // Response path
  when (io.bus.response.valid) {
    when (io.pipeline.memwrite) {
      // Perform writedata modification and send it down io.request.writedata.
      val writedata = Wire (UInt (32.W))

      // When not writing a whole word
      when (io.pipeline.maskmode =/= 2.U) {
        // Read in the existing piece of data at the address, so we "overwrite" only part of it
        val offset = io.pipeline.address (1, 0)
        val readdata = Wire (UInt (32.W))

        readdata := io.bus.response.bits.data

        // Mask the portion of the existing data so it can be or'd with the writedata
        when (io.pipeline.maskmode === 0.U) {
          when(offset === 0.U) {
            writedata := Cat(readdata(31,8), io.pipeline.writedata(7,0))
          }.elsewhen(offset === 1.U) {
            writedata := Cat(readdata(31,16), Cat(io.pipeline.writedata(15,8), readdata(7,0)))
          }.elsewhen(offset === 2.U) {
            writedata := Cat(readdata(31,24), Cat(io.pipeline.writedata(23,16),readdata(15,0)))
          }.otherwise {
            writedata := Cat(io.pipeline.writedata(31,24), readdata(23,0))
          }
        } .otherwise {
          when (offset === 0.U) {
            writedata := Cat(readdata(31,16),io.pipeline.writedata(15,0))
          }.otherwise {
            writedata := Cat(io.pipeline.writedata(31,16), readdata(15,0))
          }
        }
      } .otherwise {
        // Write the entire word
        writedata := io.pipeline.writedata
      }

      io.bus.request.bits.writedata := writedata
    } .elsewhen (io.pipeline.memread) {
      // Perform normal masking and sign extension on the read data
      val readdata_mask      = Wire(UInt(32.W))
      val readdata_mask_sext = Wire(UInt(32.W))

      val offset = io.pipeline.address(1,0)
      when (io.pipeline.maskmode === 0.U) {
        // Byte
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xff.U
      } .elsewhen (io.pipeline.maskmode === 1.U) {
        // Half-word
        readdata_mask := (io.bus.response.bits.data >> (offset * 8.U)) & 0xffff.U
      } .otherwise {
        readdata_mask := io.bus.response.bits.data
      }

      when (io.pipeline.sext) {
        when (io.pipeline.maskmode === 0.U) {
          // Byte sign extension
          readdata_mask_sext := Cat(Fill(24, readdata_mask(7)),  readdata_mask(7, 0))
        } .elsewhen (io.pipeline.maskmode === 1.U) {
          // Half-word sign extension
          readdata_mask_sext := Cat(Fill(16, readdata_mask(15)), readdata_mask(15, 0))
        } .otherwise {
          // Word sign extension (does nothing)
          readdata_mask_sext := readdata_mask
        }
      } .otherwise {
        readdata_mask_sext := readdata_mask
      }

      io.pipeline.readdata := readdata_mask_sext
    }
  }
}

class IMemCombinationalPortForAXI extends BaseIMemPortForAXI {
  when (io.axi.read.enable) {
    val request = Wire(new Request)
    request.address := io.axi.read.addr
    request.operation := Read
    request.writedata := 0.U

    io.bus.request.bits := request
    io.bus.request.valid := true.B
  }.otherwise {
    io.bus.request.valid := false.B
  }

  io.axi.read.valid := io.bus.response.valid
  io.axi.read.data := io.bus.response.bits.data
}

class DMemCombinationalPortForAXI extends BaseDMemPortForAXI {
  when (io.axi.read.enable || io.axi.write.enable) {
    // assert not reading and writing at the same time
    assert(!(io.axi.read.enable && io.axi.write.enable))

    io.bus.request.valid := true.B

    when (io.axi.read.enable) {
      io.bus.request.bits.address := io.axi.read.addr
      io.bus.request.bits.operation := Read
      // no need to put in dontcare here, as it's taken care
      // of in the base part of data mem
    } .otherwise {
      io.bus.request.bits.address := io.axi.write.addr
      io.bus.request.bits.operation := ReadWrite
      // the write data is not passed here
//      io.bus.request.bits.writedata := io.axi.write.data
      //TODO: what is select in axi
    }
  } .otherwise {
    io.bus.request.valid := false.B
  }

  when (io.bus.response.valid) {
    // when it is a write , write back the modified data
    when (io.axi.write.enable) {

      val writeData = Wire(UInt(32.W))
      val readData = Wire(UInt(32.W))

      readData := io.bus.response.bits.data
      writeData := Cat( (0 until 4) map (i => Mux(io.axi.write.sel(i), writeData(7 + 8*i, 8*i), readData(7 + 8*i, 8*i))))

      io.bus.request.bits.writedata := writeData
      io.axi.write.valid := io.bus.response.valid
    }.elsewhen(io.axi.read.enable) {
      // when it is a read, pass back the data that's being read

      //subword read does not occur here
      // sign extension and subword read ignored
      io.axi.read.data := io.bus.response.bits
      io.axi.read.valid := io.bus.response.valid
    }
  }
}
