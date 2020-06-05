package cpu.common

trait CP0Constants {
  val con_Index = CP0Struct(0)
  val con_EntryLo0 = CP0Struct(2)
  val con_EntryLo1 = CP0Struct(3)
  val con_PageMask = CP0Struct(5)
  val con_BadVAddr = CP0Struct(8)
  val con_Count = CP0Struct(9)
  val con_EntryHi = CP0Struct(10)
  val con_Status = CP0Struct(12)
  val con_Cause = CP0Struct(13)
  val con_EPC = CP0Struct(14)
}

class CP0Struct(val addr: Int, val sel: Int) {
  require(addr >= 0 && addr <= 31 && sel >= 0 && sel <= 8)
  val index = addr * 8 + sel
}

object CP0Struct{
  def apply(addr: Int, sel: Int = 0): CP0Struct = new CP0Struct(addr, sel)
}

