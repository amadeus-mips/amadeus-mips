package cpu

class CPUConfig(val build: Boolean, val memoryFile: String = "", val compareRamDirectly: Boolean = false) {
  val branchPredictorAddrLen = 10
  val branchPredictorTableEntryNum = 64
//  require(build && memoryFile.isEmpty || !build && !memoryFile.isEmpty)
}

object CPUConfig{
  val Build = new CPUConfig(build = true, memoryFile = "", compareRamDirectly = false)
}
