package cpu.common
{

  case class PhoenixConfiguration() {
    // in case some genius want to switch to mips64
    val regLen = 32
    val memWidth = 32
  }

}