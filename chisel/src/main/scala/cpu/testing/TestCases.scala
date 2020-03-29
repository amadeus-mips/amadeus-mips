package cpu.testing

object InstructionTests {

  val maxInt = BigInt("FFFFFFFF", 16)

  val rtype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "add",
      100,
      Map(5 -> 1234, 8 -> 1248),
      Map(0 -> 0, 5 -> 1234, 8 -> 1248, 9 -> 2482),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "and",
      100,
      Map(1 -> 124, 3 -> 212),
      Map(0 -> 0, 2 -> 84, 4 -> 84),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "or",
      100,
      Map(9 -> 834, 12 -> 4482),
      Map(0 -> 0, 4 -> 5058),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "srav",
      100,
      Map(10 -> -2, 11 -> 1),
      Map(0 -> 0, 9 -> twoscomp(-1)),
      Map(),
      Map()
    ),
    CPUTestCase(
      "arith",
      "sra",
      100,
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
      100,
      Map(8 -> 2, 9 -> 2, 10 -> 10),
      Map(0 -> 0, 2 -> 11, 3 -> 12),
      Map(),
      Map()
    ),
    CPUTestCase(
      "branch",
      "branchFalse",
      100,
      Map(),
      Map(0 -> 0, 2 -> 12, 3 -> 11),
      Map(),
      Map()
    )
  )

  val hazardTest = List[CPUTestCase](
    // 2 R type bypassing
    CPUTestCase(
      "bypass",
      "memToEXRR",
      100,
      Map(),
      Map(0 -> 0, 4 -> 7),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXRR",
      100,
      Map(),
      Map(0 -> 0, 4 -> 7),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXRM",
      100,
      Map(),
      Map(0 -> 0, 3 -> 5, 5 -> 5),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXRM",
      100,
      Map(),
      Map(0 -> 0, 3 -> 5, 5 -> 5),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXMM",
      100,
      Map(),
      Map(0 -> 0, 6 -> 5),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXMM",
      100,
      Map(),
      Map(0 -> 0, 6 -> 5),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "stall",
      100,
      Map(),
      Map(0 -> 0, 6 -> 5),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXAddr",
      100,
      Map(),
      Map(0 -> 0, 5 -> 300, 6 -> 300),
      Map(),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXAddr",
      100,
      Map(),
      Map(0 -> 0, 5 -> 300, 6 -> 300),
      Map(),
      Map()
    )
  )
  val randTest = List[CPUTestCase](
    CPUTestCase(
      "testBench",
      "t1",
      200,
      Map(),
      Map(0 -> 0, 2 -> 1, 3 -> 12, 4 -> 1, 7 -> twoscomp(-4)),
      Map(),
      Map()
    )
  )

  val cp0Tests = List[CPUTestCase](
    CPUTestCase(
      "cpZero",
      "cp0read",
      100,
      Map(),
      Map(0 -> 0, 1 -> BigInt("3217032064", 10), 2 -> 2, 3 -> 4259585),
      Map(),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0write",
      100,
      Map(),
      Map(0 -> 0, 2 -> 12),
      Map(),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0bypass",
      100,
      Map(),
      Map(0 -> 0, 2 -> 12),
      Map(),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0stall",
      100,
      Map(),
      Map(0 -> 0, 4 -> 25),
      Map(),
      Map()
    )
  )

  val itype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "addi",
      100,
      Map(5 -> 1234),
      Map(0 -> 0, 5 -> 1235),
      Map(),
      Map()
    )
  )

  val jtype = List[CPUTestCase](
    CPUTestCase(
      "jump",
      "jal",
      100,
      Map(),
      Map(0 -> 0, 2 -> 5, 31 -> 12),
      Map(),
      Map()
    )
  )
  val tests =
    Map(
      "rtype" -> rtype,
      "itype" -> itype,
      "branchType" -> branchType,
      "randTest" -> randTest,
      "jtype" -> jtype,
      "hazardTest" -> hazardTest,
      "cp0Tests" -> cp0Tests
    )
  val allTests = rtype ++ itype ++ branchType ++ randTest ++ jtype ++ hazardTest ++ cp0Tests

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
