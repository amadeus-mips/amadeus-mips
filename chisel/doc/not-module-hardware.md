# non-module hardware in chisel

## This is **DEPRECATED** now, please use methods in companion objects

## chisel.util.Counter example
an example from the official library
```scala
import chisel3._
import chisel3.util.Counter

class Foo extends Module {
  val io = IO(new Bundle {
    val inc = Input(Bool())
    val counter = Output(UInt(32.W))
  })
  val c = Counter(13)
  when (io.inc) { 
    c.inc()
  }
  io.counter := c.value
}
```
in this example, there are no IO for `Counter`. In fact, it is not a `Module` at all.

## key takeaway
### another example
```scala
package shared.LRU

import chisel3._
import chisel3.internal.naming.chiselName

@chiselName
class TrueLRUNM(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false) extends BaseLRUNM(numOfSets, numOfWay, searchOrder) {
  require(numOfWay == 2, "number of way should not be 2. We have a true LRU for 2")

  val lruLine = RegInit(VecInit(Seq.fill(numOfSets)(0.U(1.W))))

  override def update(index: UInt, way: UInt): Unit = {
    assert(way.getWidth == 1, "true LRU should have a way width of 1")
    lruLine(index) := way
  }

  override def getLRU(index: UInt): UInt = {
    lruLine(index)
  }
}

object TrueLRUNM {
  def apply(numOfSets: Int, numOfWay: Int, searchOrder: Boolean = false): TrueLRUNM = new TrueLRUNM(numOfSets, numOfWay, searchOrder)
}
```
- could use inheritance
- represents hardware
- **NO IO**
- can get parameters
- don't need default connection