package cpu.core.decode

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import cpu.core.Constants._

import scala.language.implicitConversions
import scala.util.Random

class DecodeTester extends ChiselFlatSpec {
  behavior of "Decode"

  it should "test Decode" in {
    Driver.execute(Array(), () => new Decode()) {
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
  val r = new Random
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
  })

  //  poke(de.io.reg1.readData, 23.U)
  //  poke(de.io.reg2.readData, 8.U)

  //  expect(de.io.out.reg1, 23)
  //  expect(de.io.out.reg2, 8)
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
  /**
   * rs, rt, imm => instName, inst, aluOp, immExt, unsignedALU, writeEnable
   */
  type I_instFunc = (Int, Int, BigInt) => (String, String, BigInt, BigInt, Boolean, Boolean)
  type J_instFunc = BigInt => (String, String, BigInt, Boolean)

  def ADD: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100000"
    val aluOp = ALU_ADD.litValue()
    val USType = US_S.litToBoolean
    ("ADD", inst, aluOp, USType, true)
  }
  def ADDU: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100001"
    val aluOp = ALU_ADD.litValue()
    val USType = US_U.litToBoolean
    ("ADDU", inst, aluOp, USType, true)
  }
  def AND: R_instFunc = (rs: Int, rt: Int, rd: Int, sa: Int) => {
    paramCheck(Seq(rs, rt, rd))
    val inst = "b000000" + rs.tb(5) + rt.tb(5) + rd.tb(5) + "00000" + "100100"
    val aluOp = ALU_AND.litValue()
    val USType = US_X.litToBoolean
    ("AND", inst, aluOp, USType, true)
  }
  val R_golden = Array(ADD, ADDU, AND)

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

  // not implemented
  def BEQ = (rs: Int, rt: Int, imm: BigInt, pc: BigInt, opData1: BigInt, opData2: BigInt) => {
    paramCheck(Seq(rs, rt), Seq(imm), Seq(pc, opData1, opData2))
    val inst = "b000100" + rs.tb(5) + rt.tb(5) + imm.tb(16)
    val branchFlag = opData1 == opData2
    val branchTarget = BTarget(pc, inst)
    ???
  }

  private def BTarget(pc: BigInt, instBin: String): BigInt = {
    pc + 4 + ("b" + List.fill(14)(instBin(16)).mkString("") + instBin.substring(16) + "00").U.litValue
  }

  private def immHighZeroExtend(imm: BigInt): BigInt = {
    imm
  }

  private def immHighSignedExtend(imm: BigInt): BigInt = {
    val immBin = imm.tb(16)
    ("b" + List.fill(16)(immBin(0)).mkString("") + immBin).U.litValue
  }

  private def paramCheck(lim5bit: Seq[Int], lim16bit: Seq[BigInt] = Seq(), lim32bit: Seq[BigInt] = Seq()): Unit = {
    val lim5bitV = 1 << 5
    lim5bit.foreach(v => {
      require(v >= 0 && v < lim5bitV, s"v is $v")
    })
    paramCheck(lim16bit, 16)
    paramCheck(lim32bit, 32)
  }

  private def paramCheck(s: Seq[BigInt], w: Int): Unit = {
    val lim = BigInt(1) << w
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

  def tb(w: Int): String = {
    // 首先去除前导0
    val re = toBinDigits(s).replaceFirst("^0*", "")
    require(re.length <= w, s"number $s is too big to convert to $w bits")
    if (re.length == w) re
    else List.fill(w - re.length)("0").mkString("") + re
  }
}

