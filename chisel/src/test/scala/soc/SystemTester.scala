package soc

import chisel3.iotesters.PeekPokeTester

class SystemTester(c: SocUpTop)(implicit cfg: SystemTestConfig) extends PeekPokeTester(c) {
  val nioReader: NIOReader = new SocketReader(9654)

  var pc:    BigInt = peek(c.io.debug.wbPC)
  var wen:   BigInt = peek(c.io.debug.wbRegFileWEn)
  var wnum:  BigInt = peek(c.io.debug.wbRegFileWNum)
  var wdata: BigInt = peek(c.io.debug.wbRegFileWData)

  def updateDebug() = {
    pc    = peek(c.io.debug.wbPC)
    wen   = peek(c.io.debug.wbRegFileWEn)
    wnum  = peek(c.io.debug.wbRegFileWNum)
    wdata = peek(c.io.debug.wbRegFileWData)
  }

  // start run
  var iCount = 0
  var cCount = 1 // avoid divide by 0

  def init(): Unit = {
    updateDebug()
  }

  def run(): Unit = {
    while(true){
      if(pc != 0){
        iCount += 1
      }
    }
    cCount += 1
    update(1)
  }

  def update(n: Int): Unit = {
    updateDebug()

    step(1)
  }



  def uartWriteSimu(): Unit = {

  }

  def uartReadSimu(): Unit = {

  }

}
