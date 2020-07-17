package soc

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.TimeUnit

trait NIOWrapper {
  def next: Option[Byte]
  def send(bytes: Seq[Byte])
}

class FakeNIO extends NIOWrapper {
  override def next: Option[Byte] = {
    if (math.random() < 0.5) {
      None
    } else {
      Some(10.toByte)
    }
  }

  override def send(bytes: Seq[Byte]): Unit = {}
}

class SocketWrapper(port: Int) extends NIOWrapper {
  val server = ServerSocketChannel.open()
  server.bind(new InetSocketAddress(port))
  println(s"server listening on $port...")
  val buf = ByteBuffer.allocate(200)
  buf.flip()
  val socket = server.accept()
  socket.configureBlocking(false)
  println("connection established")

  override def next: Option[Byte] = {
    if (buf.hasRemaining) {
      Some(buf.get())
    } else {
      buf.flip()
      val bytesRead = socket.read(buf)
      if (bytesRead > 0) Some(buf.get())
      else None
    }
  }

  override def send(bytes: Seq[Byte]): Unit = {
    socket.write(ByteBuffer.wrap(bytes.toArray))
  }
}

object NIOWrapperTest extends App {
  val wrapper = new SocketWrapper(9965)
  println("inited")
  wrapper.send("MONITOR for MIPS32 - initialized.".toCharArray.map(_.toByte))
  while (true) {
    wrapper.next match {
      case Some(b) => println(s"get data ${b.toChar}")
      case None =>
        println("sleeping")
        TimeUnit.SECONDS.sleep(1)
    }
  }
}
