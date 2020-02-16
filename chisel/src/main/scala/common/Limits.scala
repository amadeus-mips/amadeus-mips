package common

object Limits {
  val MAX32BIT: BigInt = BigInt(1) << 32
  val MAX5BIT: Int = 1 << 5
  def MAXnBIT(m: Int): BigInt = BigInt(1) << m
}
