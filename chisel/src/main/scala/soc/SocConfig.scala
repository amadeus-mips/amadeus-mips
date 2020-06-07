package soc

sealed trait DelayType

object DelayType {

  case object NoDelay extends DelayType

  case object StaticDelay extends DelayType

  case object RandomDelay extends DelayType

}

/**
  * When simulation is true, delayType should be NoDelay
  * @param simulation in simulation environment
  * @param memFile the memory file
  * @param performanceMonitor whether enable the performance monitor
  * @param delayType the ram delay type
  */
class SocConfig(
  val simulation:         Boolean,
  val memFile:            String,
  val performanceMonitor: Boolean = false,
  val delayType:          DelayType
) {
  require(!(simulation && (delayType != DelayType.NoDelay)))

}

object SocConfig {

  def funcConfig(
    simulation:         Boolean,
    performanceMonitor: Boolean   = false,
    delayType:          DelayType = DelayType.RandomDelay
  ): SocConfig = {
    new SocConfig(
      simulation,
      memFile = "./src/test/resources/loongson/func/inst_ram.coe",
      performanceMonitor,
      if (simulation) DelayType.NoDelay else delayType
    )
  }

  def perfConfig(
    simulation:         Boolean,
    performanceMonitor: Boolean   = false,
    delayType:          DelayType = DelayType.StaticDelay
  ): SocConfig = {
    new SocConfig(
      simulation,
      memFile = "./src/test/resources/loongson/perf/axi_ram.coe",
      performanceMonitor,
      if (simulation) DelayType.NoDelay else delayType
    )
  }

  def tlbConfig(
    simulation:         Boolean,
    performanceMonitor: Boolean   = false,
    delayType:          DelayType = DelayType.RandomDelay
  ): SocConfig = {
    new SocConfig(
      simulation,
      memFile = "./src/test/resources/loongson/tlb/inst_ram.coe",
      performanceMonitor,
      if(simulation) DelayType.NoDelay else delayType
    )
  }
}
