package soc

import java.nio.ByteBuffer

trait NIOReader {
  def next: Option[Byte]
}

class FakeReader extends NIOReader {
  val byteBuffer = ByteBuffer.allocate(20)
  override def next: Option[Byte] = {
    if(math.random() < 0.5){
      None
    } else {
      Some(byteBuffer.get())
    }
  }
}



