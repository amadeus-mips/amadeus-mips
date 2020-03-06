package cpu.components

import chisel3._

object CPZeroTestMain extends App {
  iotesters.Driver.execute(args, () => new CPZero) { c => new CPZeroUnitTest(c) }
}

object CPZeroRepl extends App {
  iotesters.Driver.executeFirrtlRepl(args, () => new CPZero)
}
