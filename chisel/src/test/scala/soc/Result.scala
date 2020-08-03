package soc

import java.text.SimpleDateFormat
import java.util.Date

sealed trait Result {
  val pass: Boolean
  val timestamp = System.currentTimeMillis()
  val timeString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date(timestamp))
}

class JSONString(val underlying: String)

case class PerfResult(log: String) extends Result {

  /**
    * example: the reverse of
    * "
    * bitcount PASS!Bits: 811
    * bitcount: Total Count(SoC count) = 0x57036
    * bitcount: Total Count(CPU count) = 0x2b80d
    * "
    */
  private val tail3Lines = log.split('\n').reverse.slice(0, 3)
  private val firstLine  = log.split('\n')(0)

  val name = firstLine.trim.split("test")(0).trim

  val pass: Boolean = {
    val pass = tail3Lines(2).contains("PASS")
    pass && PerfResult.gs132.contains(name)
  }

  val socCount: Long = {
    if (pass) {
      val line = tail3Lines(1)
      PerfResult.tryToLong(line.split(' ').map(_.trim).reverse.head.substring(2), 16).getOrElse(0L)
    } else {
      0L
    }
  }

  val cpuCount: Long = {
    if (pass) {
      val line = tail3Lines(0)
      PerfResult.tryToLong(line.split(' ').map(_.trim).reverse.head.substring(2), 16).getOrElse(0L)
    } else {
      0L
    }
  }

  val score: Double = {
    val gs132Count = scala.util.Try(PerfResult.gs132(name)).toOption.getOrElse(BigInt(0))
    if (socCount == 0 || gs132Count == 0) {
      0.1
    } else {
      gs132Count.toDouble / socCount.toDouble
    }
  }

  val map = scala.collection.mutable.Map[String, String]()

  /** add custom item to the result json, if want to use nested JSON, use [[JSONString]]
    *
    * @param tag tag
    * @param value if the value is instance of String, it shouldn't have special character such as "\n" or "\""
    */
  def add(tag: String, value: String): Unit = {
    map(tag) = s""""$value""""
  }

  def add(tag:String, value: JSONString): Unit = {
    map(tag) = value.underlying
  }

  def add(tag: String, value: Int): Unit = {
    map(tag) = value.toString
  }
  def add(tag: String, value: Long): Unit ={
    map(tag) = value.toString
  }
  def add(tag: String, value: Float): Unit = {
    map(tag) = value.toString
  }
  def add(tag: String, value: Double): Unit = {
    map(tag) = value.toString
  }
  def add(tag: String, value: BigInt): Unit = {
    map(tag) = value.toString
  }
  def add(tag: String, value: BigDecimal): Unit = {
    map(tag) = value.toString
  }

  def additionInformation: String = {
    "{" + map.map(e => s""""${e._1}": ${e._2}""").mkString(", ") + "}"
  }

  override def toString: String =
    s"""{ "name": "$name", "pass": $pass, "score": $score, "socCount": $socCount, "cpuCount": $cpuCount, "time": "$timeString", "addition": $additionInformation }""".stripMargin

}

object PerfResult {
  def tryToLong(s: String, radix: Int = 10): Option[Long] = {
    scala.util.Try(BigInt(s, radix).toLong).toOption
  }
  val gs132 = Map(
    "bitcount"      -> BigInt("13CF7FA", 16),
    "bubble sort"   -> BigInt("7BDD47E", 16),
    "coremark"      -> BigInt("10CE6772", 16),
    "crc32"         -> BigInt("AA1AA5C", 16),
    "dhrystone"     -> BigInt("1FC00D8", 16),
    "quick sort"    -> BigInt("719615A", 16),
    "select sort"   -> BigInt("6E0009A", 16),
    "sha"           -> BigInt("74B8B20", 16),
    "stream copy"   -> BigInt("853B00", 16),
    "string search" -> BigInt("50A1BCC", 16)
  )
}

@deprecated
case class FuncResult(pass: Boolean) extends Result {}
