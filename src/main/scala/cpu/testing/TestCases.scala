package cpu.testing

object InstructionTests {

  val maxInt = BigInt("FFFFFFFF", 16)

  val rtype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "add",
      Map("single-cycle" -> 1, "pipelined" -> 6),
      Map(5 -> 1234, 8 -> 1248),
      Map(0 -> 0, 5 -> 1234, 8 -> 1248, 9 -> 2482),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "and",
      Map("single-cycle" -> 2, "pipelined" -> 7),
      Map(1 -> 124, 3 -> 212),
      Map(0 -> 0, 2 -> 84, 4 -> 84),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "or",
      Map("single-cycle" -> 2, "pipelined" -> 7),
      Map(9 -> 834, 12 -> 4482),
      Map(0 -> 0, 4 -> 5058),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "srav",
      Map("pipelined" -> 5),
      Map(10 -> -2, 11 -> 1),
      Map(0 -> 0, 9 -> twoscomp(-1)),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "sra",
      Map("pipelined" -> 5),
      Map(2 -> -2),
      Map(0 -> 0, 3 -> twoscomp(-1)),
      Map(),
      Map()
    )
  )
  val branchType = List[CPUTestCase](
    CPUTestCase(
      "branch",
      "branchTrue",
      Map("single-cycle" -> 10, "pipelined" -> 20),
      Map(8 -> 2, 9 -> 2, 10 -> 10),
      Map(0 -> 0, 10 -> 20),
      Map(),
      Map()
    ),
    CPUTestCase(
      "branch",
      "branchFalse",
      Map("single-cycle" -> 10, "pipelined" -> 20),
      Map(8 -> 2, 9 -> 3, 10 -> 10),
      Map(0 -> 0, 10 -> 30),
      Map(),
      Map()
    ),
    CPUTestCase(
      "branch",
      "branchBoth",
      Map("pipelined" -> 40),
      Map(12 -> 20, 8 -> 1, 9 -> 1, 10 -> 15),
      Map(0 -> 0, 12 -> 28),
      Map(),
      Map()
    )
  )
  val itype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "addi",
      Map("single-cycle" -> 1, "pipelined" -> 5),
      Map(5 -> 1234),
      Map(0 -> 0, 5 -> 1235),
      Map(),
      Map()
    )
  )
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
