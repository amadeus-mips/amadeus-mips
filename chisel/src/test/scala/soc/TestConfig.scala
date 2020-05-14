package soc

/**
  *
  * @param banLog Ban the log
  * @param needAssert Need to assert while test failed (can't generate vcd)
  * @param runAllPerf Run all perf test
  * @param performanceMonitorEnable Enable the performance monitor
  * @param trace Enable the trace comparison
  * @param writeTrace Enable write trace
  * @param vcdOn Enable generate vcd file
  */
class TestConfig(
  val banLog:                   Boolean = false,
  val needAssert:               Boolean = false,
  val runAllPerf:               Boolean = false,
  val performanceMonitorEnable: Boolean = false,
  val trace:                    Boolean = false,
  val writeTrace:               Boolean = false,
  val vcdOn:                    Boolean = false
) {

  def check(perfNumber: Int) = {
    require(perfNumber >= 0 && perfNumber <= 10, "perfNumber should in 0~10")
    require(
      !(runAllPerf && vcdOn),
      "Run all perf test shouldn't generate vcd file"
    ) // or you like the 50G vcd file
    require(!(runAllPerf && writeTrace), "Run all perf test shouldn't write a trace")
    require(!(runAllPerf && trace), "Run all perf test shouldn't compare with a trace")
    require(!(writeTrace && trace), "Can't write or read trace together")
    require(!(writeTrace && perfNumber == 0), "Write trace should specify the perf number")
  }

}
