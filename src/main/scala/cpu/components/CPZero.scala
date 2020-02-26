package cpu.components

import chisel3._
import chisel3.util._
import cpu.utils.exceptionVector

//object exceptionCodes {
//  val interrupt :: mod :: tlbl :: tlbs :: adel :: ades :: ibe :: dbe :: syscall :: bp :: ri :: cpu :: ov :: trap :: unused0 :: fpe :: cus0 :: cus1 :: c2e :: expand0 :: expand1 :: expand2 :: mdmx :: watch :: mcheck :: thread :: dsp :: expand3 :: expand4 :: expand5 :: cacherr :: unused1 :: Nil =
//    Enum(32)
//}

// this passed the compilation test instead of the one above, one would infer that you'd have to use them
object excCodes {
  val ibe :: ri :: ov :: dbe :: Nil = Enum(4)
}

class CPZeroIn extends Bundle {

  //TODO: merge the io

  // support for reading from cop0
  val readEnable = Input(Bool())

  // support for writing to cop0
  val isException = Input(Bool())
  val cause = Input(new exceptionVector)
  val memException = Input(Bool())
  val pcPlusFour = Input(UInt(32.W))
}

class CPZeroOut extends Bundle {
  // whether jump to the exception handler ( when enter the exception ) or epc ( exiting exception )
  val jToNextPC = Output(Bool())
  // what the next pc will be. This will be ignored if jToNextPC is false
  val nextPC = Output(UInt(32.W))
}

//TODO: initialization values

import cpu.components.excCodes._

//TODO: this is assuming no multiple exceptions

class CPZero extends Module {
  val io = IO(new Bundle() {
    val input = new CPZeroIn
    val output = new CPZeroOut
  })

  io.output.jToNextPC := false.B
  io.output.nextPC := DontCare // don't care about the next PC by default

  //TODO: add reginit value for mips32 compliance ( instead of for competition ), some of the specifications
  // for the competition is not mips32-compliant

  // register 9
  // count register, work with compare ( 11 )
  val regCount = RegInit(0.U(33.W))
  regCount := regCount + 1.U
  // when using, use regCount(32,1)

  // register 11
  // compare register, work with count ( 9 )
  val regCompare = RegInit(0.U(32.W))

  // register 12
  // initialize the status register
  // initialization value: 00000000010000001111111100000001
  val regSR = RegInit(4259585.U(32.W))

  // register 13
  // cause of exception ( interrupt )
  val regCause = RegInit(0.U(32.W))

  // register 14
  // initialize the epc register
  val regEPC = RegInit(0.U(32.W))

  // register 15 select 1
  // the base register for exception handlers
  // TODO: ram entry point: 0x800000180
  //TODO: for the compliance of the competition, ebase is ignored ( fixed to a pre-defined value ) because bev is always 1
  // exception handler is always at 0xBFC00380, which is 0xBFC0.0200 + 0x180
  //TODO: will long literal compile?
  val regEBase = RegInit(3217032064L.U(32.W))

  // when there is an exception and exception level is 0
  when(io.input.isException && (regSR(1) === 0.U)) {
    //1. changing exception level to one means automatically it's kernel mode, see mips32 privileged page 22
    regSR := Cat(regSR(31, 2), 3.U(2.W))

    //2. setup the actual cause for the exception
    when(io.input.cause.fetchException) {
      //TODO: this does not comply with standards
      //TODO: what to do when multiple exceptions occur

      regCause := Cat(io.input.cause.isBranchDelaySlot, Fill(25, 0.U(1.W)), ibe, Fill(2, 0.U(1.W)))
    }.elsewhen(io.input.cause.decodeException) {
        regCause := Cat(io.input.cause.isBranchDelaySlot, Fill(25, 0.U(1.W)), ri, Fill(2, 0.U(1.W)))
      }
      .elsewhen(io.input.cause.overflowException) {
        regCause := Cat(io.input.cause.isBranchDelaySlot, Fill(25, 0.U(1.W)), ov, Fill(2, 0.U(1.W)))
      }
      .elsewhen(io.input.memException) {
        // TODO: multiple causes
        regCause := Cat(io.input.cause.isBranchDelaySlot, Fill(25, 0.U(1.W)), dbe, Fill(2, 0.U(1.W)))
      }

    //3. set up the branch delay slot
    when(io.input.cause.isBranchDelaySlot) {
      regEPC := io.input.pcPlusFour - 8.U
    }.otherwise {
      regEPC := io.input.pcPlusFour - 4.U
    }
    //4. return the next pc to point to the exception handler
    io.output.nextPC := regEBase
  }
  //TODO: implement eret: returning from the instruction
}
