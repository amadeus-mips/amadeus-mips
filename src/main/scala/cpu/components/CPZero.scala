package cpu.components

import chisel3._
import chisel3.util._

//TODO: Didn't compile
//object exceptionCodes {
//  val interrupt :: mod :: tlbl :: tlbs :: adel :: ades :: ibe :: dbe :: syscall :: bp :: ri :: cpu :: ov :: trap :: unused0 :: fpe :: cus0 :: cus1 :: c2e :: expand0 :: expand1 :: expand2 :: mdmx :: watch :: mcheck :: thread :: dsp :: expand3 :: expand4 :: expand5 :: cacherr :: unused1 :: Nil =
//    Enum(32)
//}

//TODO: get the values right
object excCodes {
  val ibe :: ri :: ov :: dbe :: syscall :: bp :: Nil = Enum(6)
}

class CPZeroIn extends Bundle {

  val readEnable = Input(Bool())

  val cause = Input(UInt(5.W))
  // is branch delay slot
  val isBD = Input(Bool())
  val pcPlusFour = Input(UInt(32.W))
}

class CPZeroOut extends Bundle {
  val jToNextPC = Output(Bool())
  val nextPC = Output(UInt(32.W))
}

//TODO: initialization values

import cpu.components.excCodes._

//TODO: implement nested exceptions

class CPZero extends Module {
  val io = IO(new Bundle() {
    val input = new CPZeroIn
    val output = new CPZeroOut
  })

  //TODO: add reginit value for mips32 compliance ( instead of for competition ), some of the specifications
  // for the competition is not mips32-compliant

  // register 9
  // when using, use regCount(32,1)
  val regCount = RegInit(0.U(33.W))
  regCount := regCount + 1.U

  // register 11
  val regCompare = RegInit(0.U(32.W))

  // register 12
  // initialization value: 00000000010000001111111100000001
  val regSR = RegInit(4259585.U(32.W))

  // register 13
  val regCause = RegInit(0.U(32.W))

  // register 14
  val regEPC = RegInit(0.U(32.W))

  // register 15 select 1
  // the base register for exception handlers
  // TODO: ram entry point: 0x800000180
  // TODO: for the compliance of the competition, ebase is ignored ( fixed to a pre-defined value ) because bev is always 1
  // exception handler is always at 0xBFC00380, which is 0xBFC0.0200 + 0x180
  val regEBase = RegInit(3217032064L.U(32.W))

  io.output.jToNextPC := false.B
  io.output.nextPC := regEPC

  val isEret = Wire(Bool())
  isEret := io.input.cause === 3.U
  val isException = Wire(Bool())
  isException := io.input.cause.orR.asBool

  // when there is an exception
  when(isException && regSR(1).asBool()) {

    // assuming the instruction eret cannot cause other exceptions
    regSR := Mux(
      isEret,
      Cat(regSR(31, 2), 0.U(1.W), regSR(0)),
      Cat(regSR(31, 2), 1.U(1.W), regSR(0))
    )

    //2. setup the actual cause for the exception
    //TODO: this does not comply with standards

    regCause := MuxLookup(
      io.input.cause,
      regCause,
      Array(
        1.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), bp, Fill(2, 0.U(1.W))),
        2.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), syscall, Fill(2, 0.U(1.W))),
        3.U -> regCause,
        4.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), ibe, Fill(2, 0.U(1.W))),
        5.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), ri, Fill(2, 0.U(1.W))),
        6.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), ov, Fill(2, 0.U(1.W))),
        7.U -> Cat(io.input.isBD, Fill(25, 0.U(1.W)), dbe, Fill(2, 0.U(1.W)))
      )
    )

    // set up the pc selection
    io.output.jToNextPC := isException
    io.output.nextPC := Mux(isEret, regEPC, regEBase)
  }
}
