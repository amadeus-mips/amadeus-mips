package cpu.core.decode

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import cpu.core.Constants._
import common.{Limits, Util}

import scala.language.implicitConversions
import scala.util.Random

class DecodeTester extends ChiselFlatSpec {
  behavior of "Decode"

  it should "test Decode" in {
    Driver.execute(Array(), () => new Decode()) {
      c => new DecodeUnitTester(c)
    } should be(true)
  }
  "using verilator" should "generate vcd file" in {
    Driver.execute(Array("--backend-name", "verilator"), () => new Decode()) {
      c => new DecodeUnitTester(c)
    } should be(true)
  }
}

class DecodeUnitTester(c: Decode) extends PeekPokeTester(c) {

  import DecodeUnitTester._

  private val de = c
  // 无数据前推测试
  poke(de.io.exWR.writeEnable, false)
  poke(de.io.memWR.writeEnable, false)

  var a = 0
//  val seed = System.currentTimeMillis()
    val seed = 1581058598703L
  println(s"random seed: $seed")
  val r = new Random(seed)
  R_golden.foreach(f => {
    val rs = r.nextInt(1 << 5)
    val rt = r.nextInt(1 << 5)
    val rd = r.nextInt(1 << 5)
    val sa = r.nextInt(1 << 5)
    val (name, inst, aluOp, unsignedALU, writeEnable) = f(rs, rt, rd, sa)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.inst, inst.U(32.W))
    val opData1 = BigInt(32, r)
    val opData2 = BigInt(32, r)
    poke(de.io.reg1.readData, opData1)
    poke(de.io.reg2.readData, opData2)

    expect(de.io.out.aluOp, aluOp)
    expect(de.io.out.aluSigned, unsignedALU)
    //    expect(de.io.out.inst, inst.U(32.W))
    expect(de.io.reg1.readTarget, rs)
    expect(de.io.reg2.readTarget, rt)
    expect(de.io.out.writeRegister.writeTarget, rd)
    expect(de.io.out.writeRegister.writeEnable, writeEnable)
    expect(de.io.out.reg1, opData1)
    expect(de.io.out.reg2, opData2)
    //    println(s"$name test finished")
    step(1)
  })

  I_golden.foreach(f => {
    val rs = r.nextInt(1 << 5)
    val rt = r.nextInt(1 << 5)
    val imm = BigInt(16, r)
    val (name, inst, aluOp, immExt, unsignedALU, writeEnable) = f(rs, rt, imm)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.inst, inst.U(32.W))
    val opData1 = BigInt(32, r)
    val opData2 = BigInt(32, r)
    poke(de.io.reg1.readData, opData1)
    poke(de.io.reg2.readData, opData2)

    expect(de.io.out.aluOp, aluOp)
    expect(de.io.out.aluSigned, unsignedALU)
    expect(de.io.reg1.readTarget, rs)
    expect(de.io.out.writeRegister.writeTarget, rt)
    expect(de.io.out.writeRegister.writeEnable, writeEnable)
    expect(de.io.out.reg1, opData1)
    expect(de.io.out.reg2, immExt)
    //    println(s"$name test finished")
    step(1)
  })

  B_I_golden.foreach(f => {
    val rs = r.nextInt(1 << 5)
    val rt = r.nextInt(1 << 5)
    val imm = BigInt(16, r)
    val pc = BigInt(32, r)
    val opData1 = BigInt(32, r)
    val opData2 = BigInt(32, r)
    val (name, inst, branchFlag, branchTarget) = f(rs, rt, imm, pc, opData1, opData2)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.pc, pc)
    poke(de.io.ifIn.inst, inst.U(32.W))
    poke(de.io.reg1.readData, opData1)
    poke(de.io.reg2.readData, opData2)
    expect(de.io.reg1.readTarget, rs)
    expect(de.io.reg2.readTarget, rt)
    expect(de.io.out.writeRegister.writeEnable, false)
    expect(de.io.branchFlag, branchFlag)
    expect(de.io.branchTarget, branchTarget)
    step(1)
  })

  BZ_I_golden.foreach(f => {
    val rs = r.nextInt(1 << 5)
    val imm = BigInt(16, r)
    val pc = BigInt(32, r)
    val opData1 = BigInt(32, r)
    val opData2 = BigInt(32, r)
    val (name, inst, branchFlag, branchTarget, writeEnable) = f(rs, imm, pc, opData1, opData2)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.pc, pc)
    poke(de.io.ifIn.inst, inst.U(32.W))
    poke(de.io.reg1.readData, opData1)
    poke(de.io.reg2.readData, opData2)
    expect(de.io.reg1.readTarget, rs)
    if(writeEnable) expect(de.io.out.writeRegister.writeTarget, cpu.core.Constants.GPR31)
    expect(de.io.out.writeRegister.writeEnable, writeEnable)
    expect(de.io.branchFlag, branchFlag)
    expect(de.io.branchTarget, branchTarget)
    step(1)
  })

  J_golden.foreach(f => {
    val pc = BigInt(32, r)
    val index = BigInt(26, r)
    val (name, inst, branchTarget, writeEnable) = f(pc, index)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.pc, pc)
    poke(de.io.ifIn.inst, inst.U(32.W))

    if(writeEnable) expect(de.io.out.writeRegister.writeTarget, cpu.core.Constants.GPR31)
    expect(de.io.out.writeRegister.writeEnable, writeEnable)
    expect(de.io.branchFlag, true)
    expect(de.io.branchTarget, branchTarget)
    step(1)
  })

  JR_golden.foreach(f => {
    val rs = r.nextInt(1<<5)
    val rd = r.nextInt(1<<5)
    val opData1 = BigInt(32, r)
    val (name, inst, branchTarget, writeEnable) = f(rs, rd, opData1)
    println(s"$a. $name testing")
    a += 1
    poke(de.io.ifIn.inst, inst.U(32.W))
    poke(de.io.reg1.readData, opData1)

    if(writeEnable) expect(de.io.out.writeRegister.writeTarget, rd)
    expect(de.io.out.writeRegister.writeEnable, writeEnable)
    expect(de.io.branchFlag, true)
    expect(de.io.branchTarget, branchTarget)
    step(1)
  })

  //  poke(de.io.reg1.readData, 23.U)
  //  poke(de.io.reg2.readData, 8.U)

  //  expect(de.io.out.reg1, 23)
  //  expect(de.io.out.reg2, 8)
  step(1)
}

object DecodeUnitTester {
  /**
   * to simplify Int transfer to binary string with leading 0 <br/>
   * {{{ 5.tb(4) ==> "0101" }}}
   *
   * @param s source
   * @return
   */
  implicit def intToBinary(s: Int) = new IntToBinaryHelper(s)

  implicit def bigIntToBinary(s: BigInt) = new BigIntToBinaryHelper(s)

  /**
   * rs, rt, rd, sa => instName, inst, aluOp, unsignedALU, writeEnable
   */
  type R_instFunc = (Int, Int, Int, Int) => (String, String, BigInt, Boolean, Boolean)
  def ADD: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd, sa))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100000"
    val aluOp = ALU_ADD.litValue()
    val USType = US_S.litToBoolean
    ("ADD", inst, aluOp, USType, true)
  }
  def ADDU: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd, sa))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100001"
    val aluOp = ALU_ADD.litValue()
    val USType = US_U.litToBoolean
    ("ADDU", inst, aluOp, USType, true)
  }
  def AND: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd, sa))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100100"
    val aluOp = ALU_AND.litValue()
    val USType = US_X.litToBoolean
    ("AND", inst, aluOp, USType, true)
  }
  val R_golden = Array(ADD, ADDU, AND)

  /**
   * rs, rt, imm => instName, inst, aluOp, immExt, unsignedALU, writeEnable
   */
  type I_instFunc = (Int, Int, BigInt) => (String, String, BigInt, BigInt, Boolean, Boolean)
  def ADDI: I_instFunc = (rs: Int, rt: Int, imm: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm))
    val inst = "b001000" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val aluOp = ALU_ADD.litValue()
    val immExt = immHighSignedExtend(imm)
    val USType = US_S.litToBoolean
    ("ADDI", inst, aluOp, immExt, USType, true)
  }
  def ADDIU: I_instFunc = (rs: Int, rt: Int, imm: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm))
    val inst = "b001001" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val aluOp = ALU_ADD.litValue()
    val immExt = immHighSignedExtend(imm)
    val USType = US_U.litToBoolean
    ("ADDI", inst, aluOp, immExt, USType, true)
  }
  def ANDI: I_instFunc = (rs: Int, rt: Int, imm: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm))
    val inst = "b001100" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val aluOp = ALU_AND.litValue()
    val immExt = immHighZeroExtend(imm)
    val USType = US_U.litToBoolean
    ("ANDI", inst, aluOp, immExt, USType, true)
  }
  val I_golden = Array(ADDI, ANDI)

  /**
   * rs, rt, imm, pc, opData1, opData2 => name, inst, branchFlag, branchTarget
   */
  type B_I_instFunc = (Int, Int, BigInt, BigInt, BigInt, BigInt) => (String, String, Boolean, BigInt)
  def BEQ: B_I_instFunc = (rs: Int, rt: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000100" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val branchFlag = opData1 == opData2
    val branchTarget = BTarget(pc, imm)
    ("BEQ", inst, branchFlag, branchTarget)
  }
  def BNE: B_I_instFunc = (rs: Int, rt: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000101" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val branchFlag = opData1 != opData2
    val branchTarget = BTarget(pc, imm)
    ("BNE", inst, branchFlag, branchTarget)
  }
  val B_I_golden = Array(BEQ, BNE)

  /**
   * rs, imm, pc, opData1, opDat2 => name, inst, branchFlag, branchTarget, writeEnable
   */
  type BZ_I_instFunc = (Int, BigInt, BigInt, BigInt, BigInt) => (String, String, Boolean, BigInt, Boolean)
  def BGEZ: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000001" + rs.tb(5) + "00001" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) >= 0
    val branchTarget = BTarget(pc, imm)
    ("BGEZ", inst, branchFlag, branchTarget, false)
  }
  def BGEZAL: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000001" + rs.tb(5) + "10001" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) >= 0
    val branchTarget = BTarget(pc, imm)
    ("BGEZAL", inst, branchFlag, branchTarget, true)
  }
  def BGTZ: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000111" + rs.tb(5) + "00000" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) > 0
    val branchTarget = BTarget(pc, imm)
    ("BGTZ", inst, branchFlag, branchTarget, false)
  }
  def BLEZ: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000110" + rs.tb(5) + "00000" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) <= 0
    val branchTarget = BTarget(pc, imm)
    ("BLEZ", inst, branchFlag, branchTarget, false)
  }
  def BLTZ: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000001" + rs.tb(5) + "00000" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) < 0
    val branchTarget = BTarget(pc, imm)
    ("BLTZ", inst, branchFlag, branchTarget, false)
  }
  def BLTZAL: BZ_I_instFunc = (rs: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000001" + rs.tb(5) + "10000" + imm.tb(16)
    val branchFlag = Util.unsignedToSigned(opData1) < 0
    val branchTarget = BTarget(pc, imm)
    ("BLTZAL", inst, branchFlag, branchTarget, true)
  }
  val BZ_I_golden = Array(BGEZ, BGEZAL)

  /**
   * index => name, inst, branchTarget, writeEnable
   */
  type J_instFunc = (BigInt, BigInt) => (String, String, BigInt, Boolean)
  def J: J_instFunc = (pc, index) => {
    paramCheck(Seq(index), 26)
    paramCheck(Seq(pc), 32)
    val inst = "b000010" + index.tb(26)
    val Target = JTarget(pc, index)
    ("J", inst, Target, false)
  }
  def JAL: J_instFunc = (pc, index) => {
    paramCheck(Seq(index), 26)
    paramCheck(Seq(pc), 32)
    val inst = "b000011" + index.tb(26)
    val Target = JTarget(pc, index)
    ("J", inst, Target, true)
  }
  val J_golden = Array(J, JAL)

  /**
   * rs, rd, opData1 => name, inst, branchTarget, writeEnable
   */
  type JR_instFunc = (Int, Int, BigInt) => (String, String, BigInt, Boolean)
  def JR: JR_instFunc = (rs, rd, opData1) => {
    paramCheck(Seq(rs, rd))
    val inst = "b000000" + rs.tb(5) + "00000" + "00000" + "00000" + "001000"
    val Target = opData1
    ("JR", inst, Target, false)
  }
  def JALR: JR_instFunc = (rs, rd, opData1) => {
    paramCheck(Seq(rs, rd))
    val inst = "b000000" + rs.tb(5) + "00000" + rd.tb(5) + "00000" + "001001"
    val Target = opData1
    ("JALR", inst, Target, true)
  }
  val JR_golden = Array(JR, JALR)

  private def JTarget(pc: BigInt, index: BigInt): BigInt = {
    ("b" + pc.tb().substring(0,4) + index.tb(26) + "00").U.litValue()
  }

  private def BTarget(pc: BigInt, imm: BigInt): BigInt = {
    val sImm = Util.unsignedToSigned(imm, 16)
    val re = pc + 4 + (sImm << 2)
    require(re > 0, s"pc is $pc, sImm is $sImm")
    if (re >= Limits.MAX32BIT) re - Limits.MAX32BIT
    else re
  }

  private def immHighZeroExtend(imm: BigInt): BigInt = {
    imm
  }

  private def immHighSignedExtend(imm: BigInt): BigInt = {
    val immBin = imm.tb(16)
    ("b" + List.fill(16)(immBin(0)).mkString("") + immBin).U.litValue
  }

  private def paramCheck(lim5bit: Seq[Int], lim16bit: Seq[BigInt] = Seq(), lim32bit: Seq[BigInt] = Seq()): Unit = {
    lim5bit.foreach(v => {
      require(v >= 0 && v < Limits.MAX5BIT, s"v is $v")
    })
    paramCheck(lim16bit, 16)
    paramCheck(lim32bit, 32)
  }

  private def paramCheck(s: Seq[BigInt], w: Int): Unit = {
    val lim = Limits.MAXnBIT(w)
    s.foreach(v => {
      require(v >= 0 && v < lim, s"v is ${v.toString()}")
    })
  }
}


/** int to binary with leading 0.
 *
 * Thanks to  `user unknown`'s answer in
 * <a href="https://stackoverflow.com/questions/9442381/formatting-binary-values-in-scala">stackOverflow</a>
 *
 * @param s source
 */
class IntToBinaryHelper(s: Int) {
  def tb(w: Int): String = {
    val l: java.lang.Long = s.toBinaryString.toLong
    String.format("%0" + w + "d", l)
  }
}

/** BigInt to binary with leading 0.
 *
 * @see
 * [[cpu.core.decode.IntToBinaryHelper]]
 * @param s source
 */
class BigIntToBinaryHelper(s: BigInt) {
  private def toBinDigits(bi: BigInt): String = {
    if (bi == 0) "0" else toBinDigits(bi / 2) + (bi % 2)
  }

  def tb(w: Int = 32): String = {
    // 首先去除前导0
    val re = toBinDigits(s).replaceFirst("^0*", "")
    require(re.length <= w, s"number $s is too big to convert to $w bits")
    if (re.length == w) re
    else List.fill(w - re.length)("0").mkString("") + re
  }
}

