package cpu.common
{

  case class PhoenixConfiguration() {
    // in case some genius want to switch to mips64
    val regLen = 32
    val memDataWidth = 32
    val memAddressWidth = 32
    val AluOps = 5
    val memSize = 1024
  }

}
