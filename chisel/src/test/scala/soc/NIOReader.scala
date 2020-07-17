package soc

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import java.util.concurrent.TimeUnit

trait NIOReader {
  def next: Option[Byte]
}

class FakeReader extends NIOReader {
  override def next: Option[Byte] = {
    if(math.random() < 0.5){
      None
    } else {
      Some(10.toByte)
    }
  }
}

class SocketReader(port: Int) extends NIOReader {
  val server = ServerSocketChannel.open()
  server.bind(new InetSocketAddress(port))
  println(s"server listening on $port...")
  val buf = ByteBuffer.allocate(200)
  val inChannel = server.accept()
  inChannel.configureBlocking(false)
  println("connection established")

  override def next: Option[Byte] = {
    if(!inChannel.isConnected) {
      if(buf.hasRemaining){
        Some(buf.get())
      } else {
        buf.clear()
        val bytesRead = inChannel.read(buf)
        if(buf.hasRemaining) Some(buf.get())
        else None
      }
    } else {
      None
    }
  }
}

object NIOReaderTest extends App {
  val reader = new SocketReader(9965)
  println("init")
  while(true){
    reader.next match {
      case Some(b) => println(s"get data ${b.toChar}")
      case None =>
        println("sleeping")
        TimeUnit.SECONDS.sleep(1)
    }
  }
}



