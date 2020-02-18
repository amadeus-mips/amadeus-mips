package cpu.testing

object InstructionTests {

  val maxInt = BigInt("FFFFFFFF", 16)
  val rtype = List[CPUTestCase] {
    CPUTestCase(
      "arith",
      "add.txt",
      Map("single-cycle" -> 1, "pipelined" -> 5),
      Map(5 -> 1234, 8 -> 1248),
      Map(0 -> 0, 5 -> 1234, 8 -> 1248, 9 -> 2482),
      Map(),
      Map()
    )
  }
  val itype = List[CPUTestCase] {
    CPUTestCase(
      "arith",
      "addi.txt",
      Map("single-cycle" -> 1, "pipelined" -> 5),
      Map(5 -> 1234),
      Map(0 -> 0, 5 -> 1235),
      Map(),
      Map()
    )
  }
  val tests = Map("rtype" -> rtype)
  val allTests = rtype ++ itype
  val nameMap = allTests.map(x => x.name() -> x).toMap

  // 2's complement value
  def twoscomp(v: BigInt): BigInt = {
    if (v < 0) {
      maxInt + v + 1
    } else {
      v
    }
  }
}
