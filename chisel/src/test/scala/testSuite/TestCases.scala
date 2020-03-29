package testSuite

object InstructionTests {

  val maxInt = BigInt("FFFFFFFF", 16)

  val rtype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "add",
      100,
      Map(0 -> 0, 5 -> 1234, 8 -> 1248, 9 -> 2482),
      Map()
    ),
    CPUTestCase(
      "arith",
      "and",
      100,
      Map(0 -> 0, 2 -> 84, 4 -> 84),
      Map()
    ),
    CPUTestCase(
      "arith",
      "or",
      100,
      Map(0 -> 0, 4 -> 5058),
      Map()
    ),
    CPUTestCase(
      "arith",
      "srav",
      100,
      Map(0 -> 0, 9 -> twoscomp(-1)),
      Map()
    ),
    CPUTestCase(
      "arith",
      "sra",
      100,
      Map(0 -> 0, 3 -> twoscomp(-1)),
      Map()
    )
  )
  val branchType = List[CPUTestCase](
    CPUTestCase(
      "branch",
      "branchTrue",
      100,
      Map(0 -> 0, 2 -> 11, 3 -> 12),
      Map()
    ),
    CPUTestCase(
      "branch",
      "branchFalse",
      100,
      Map(0 -> 0, 2 -> 12, 3 -> 11),
      Map()
    )
  )

  val hazardTest = List[CPUTestCase](
    // 2 R type bypassing
    CPUTestCase(
      "bypass",
      "memToEXRR",
      100,
      Map(0 -> 0, 4 -> 7),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXRR",
      100,
      Map(0 -> 0, 4 -> 7),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXRM",
      100,
      Map(0 -> 0, 3 -> 5, 5 -> 5),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXRM",
      100,
      Map(0 -> 0, 3 -> 5, 5 -> 5),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXMM",
      100,
      Map(0 -> 0, 6 -> 5),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXMM",
      100,
      Map(0 -> 0, 6 -> 5),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "stall",
      100,
      Map(0 -> 0, 6 -> 5),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "memToEXAddr",
      100,
      Map(0 -> 0, 5 -> 300, 6 -> 300),
      Map()
    ),
    CPUTestCase(
      "bypass",
      "wbToEXAddr",
      100,
      Map(0 -> 0, 5 -> 300, 6 -> 300),
      Map()
    )
  )
  val randTest = List[CPUTestCase](
    CPUTestCase(
      "testBench",
      "t1",
      200,
      Map(0 -> 0, 2 -> 1, 3 -> 12, 4 -> 1, 7 -> twoscomp(-4)),
      Map()
    )
  )

  val cp0Tests = List[CPUTestCase](
    CPUTestCase(
      "cpZero",
      "cp0read",
      100,
      Map(0 -> 0, 1 -> BigInt("3217032064", 10), 2 -> 2, 3 -> 4259585),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0write",
      100,
      Map(0 -> 0, 2 -> 12),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0bypass",
      100,
      Map(0 -> 0, 2 -> 12),
      Map()
    ),
    CPUTestCase(
      "cpZero",
      "cp0stall",
      100,
      Map(0 -> 0, 4 -> 25),
      Map()
    )
  )

  val itype = List[CPUTestCase](
    CPUTestCase(
      "arith",
      "addi",
      100,
      Map(0 -> 0, 5 -> 1235),
      Map()
    )
  )

  val jtype = List[CPUTestCase](
    CPUTestCase(
      "jump",
      "jal",
      100,
      Map(0 -> 0, 2 -> 5, 31 -> 12),
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
