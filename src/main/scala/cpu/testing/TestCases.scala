package cpu.testing

object InstructionTests {

  val maxInt = BigInt("FFFFFFFF", 16)

  val rtype = List[CPUTestCase] {
    CPUTestCase(
      "arith",
      "add.txt",
      Map("single-cycle" -> 1, "pipelined" -> 6),
      Map(5 -> 1234, 8 -> 1248),
      Map(0 -> 0, 5 -> 1234, 8 -> 1248, 9 -> 2482),
      Map(),
      Map()
    )
  }
  val branchType = List[CPUTestCase](
    CPUTestCase(
      "branch",
      "branchTrue.txt",
      Map("single-cycle" -> 10, "pipelined" -> 20),
      Map(8 -> 2, 9 -> 2, 10 -> 10),
      Map(0 -> 0, 10 -> 20),
      Map(),
      Map()
    ),
    CPUTestCase(
      "branch",
      "branchFalse.txt",
      Map("single-cycle" -> 10, "pipelined" -> 20),
      Map(8 -> 2, 9 -> 3, 10 -> 10),
      Map(0 -> 0, 10 -> 30),
      Map(),
      Map()
    )
  )
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
  val tests = Map("rtype" -> rtype, "itype" -> itype, "branchType" -> branchType)
  val allTests = rtype ++ itype ++ branchType

  // 2's complement value
  def twoscomp(v: BigInt): BigInt = {
    if (v < 0) {
      maxInt + v + 1
    } else {
      v
    }
  }
  val nameMap = allTests.map(x => x.name() -> x).toMap

}
