import chisel3.Driver
import chisel3._
import common.Util

object ChiselTest {

  def main(args: Array[String]): Unit = {
    // Vivado 不认识非ASCII码，因此去掉了toString方法产生的"$@"
    val outDir: String = "./out/" + toString.replaceAll("\\$@", "")
    Driver.execute(Array("-td", outDir),  () => new Module {
      val io = IO(new Bundle() {
        val in = Input(UInt(32.W))
        val out = Output(UInt(32.W))
      })

      io.out := Util.subwordModify(io.in, (31, 1), 0.U(31.W))
    })
  }
}
