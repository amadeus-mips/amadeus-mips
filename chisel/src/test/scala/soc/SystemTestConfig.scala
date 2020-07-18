package soc

/** 1s--20000cycle */
case class SystemTestConfig(
  switchData: BigInt = BigInt("ff", 16),
  needBlockingIO: Boolean = false,
  blockingIOCycle: BigInt = BigInt(1000000),
  cycleLimit: BigInt = BigInt(1000000)
) {}
