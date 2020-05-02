package shared
import chisel3._
import chisel3.util._

class treeNode extends Bundle {
  val root = Bool()
  val upper = Bool()
  val lower = Bool()
}

class PseudoLRUTree(numOfWay: Int, numOfSets: Int) extends Module {
  require(numOfWay == 4, "number of way should not be 2. We have a true LRU for 2")
  require(isPow2(numOfWay), "number of way should be a power of 2")
  require(isPow2(numOfSets), "number of sets should be a power of 2")

  /**
    * access way is the newly accessed way
    * lru line is the output indicating which line is the lease recently used
    */
  val io = IO(new LRUIO(pNumOfSets = numOfSets, pNumOfWays = numOfWay))
  val treeP = RegInit(VecInit(Seq.fill(numOfSets)(0.U.asTypeOf(new treeNode))))
  when(io.accessEnable) {
    // when accessing line 0,1, point the root to upper ( lru is in 2,3 )
    // and point the lru pointer in 0,1 to the one not accessed
    when(!io.accessWay(1)) {
      treeP(io.accessSet).root := true.B
      treeP(io.accessSet).lower := !io.accessWay(0)
    }.otherwise {
      // accessing 2,3, lru is in lower, update 2,3 to point to the one not used
      treeP(io.accessSet).root := false.B
      treeP(io.accessSet).upper := !io.accessWay(0)
    }
  }
  io.lruLine := Mux(
    treeP(io.accessSet).root,
    Cat(1.U(1.W), treeP(io.accessSet).upper),
    Cat(0.U(1.W), treeP(io.accessSet).lower)
  )
}
