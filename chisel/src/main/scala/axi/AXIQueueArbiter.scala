package axi

import chisel3._
import chisel3.internal.naming.chiselName
import chisel3.util._
import shared.Constants

//noinspection DuplicatedCode
@chiselName
class AXIQueueArbiter(sCount: Int = 3, queueDepth: Int = 5) extends Module {
  val io = IO(new Bundle() {
    val slaves = Vec(sCount, AXIIO.slave())
    val master = AXIIO.master()
  })

  def chosen(request: Seq[Bool]): UInt = {
    val chosen = WireInit((sCount - 1).U)
    for (i <- sCount - 2 to 0 by -1) {
      when(request(i)) {
        chosen := i.U
      }
    }
    chosen
  }
  //===--------------------------------------------------------------===
  // ReadChannel
  //===--------------------------------------------------------------===
  {
    val arChosen = chosen(io.slaves.map(_.ar.valid))

    val sARIdle :: sARWaiting :: Nil = Enum(2)

    val arState        = RegInit(sARIdle)
    val arSelectLocked = RegInit(0.U(log2Ceil(sCount).W))

    switch(arState) {
      is(sARIdle) {
        when(io.slaves(arChosen).ar.valid) {
          arState        := Mux(io.master.ar.ready, sARIdle, sARWaiting)
          arSelectLocked := arChosen
        }
      }
      is(sARWaiting) {
        when(io.master.ar.ready) {
          arState := sARIdle
        }
      }
    }
    val arSelect = Mux(arState === sARIdle, arChosen, arSelectLocked)

    io.slaves
      .map(_.ar)
      .foreach(ar => {
        ar.ready := false.B
      })
    io.slaves(arSelect).ar <> io.master.ar

    val instARQ  = Module(new Queue(UInt(log2Ceil(sCount).W), queueDepth))
    val dataARQ  = Module(new Queue(UInt(log2Ceil(sCount).W), queueDepth))
    val returnRQ = Module(new Queue(new AXIDataReadBundle(), queueDepth, flow = true))

    val rSelect = Mux(returnRQ.io.deq.bits.id === Constants.INST_ID, instARQ.io.deq.bits, dataARQ.io.deq.bits)

    instARQ.io.enq.valid := io.master.ar.fire() && io.master.ar.bits.id === Constants.INST_ID
    instARQ.io.enq.bits  := arSelect
    instARQ.io.deq.ready := returnRQ.io.deq
      .fire() && returnRQ.io.deq.bits.last && returnRQ.io.deq.bits.id === Constants.INST_ID

    dataARQ.io.enq.valid := io.master.ar.fire() && io.master.ar.bits.id === Constants.DATA_ID
    dataARQ.io.enq.bits  := arSelect
    dataARQ.io.deq.ready := returnRQ.io.deq
      .fire() && returnRQ.io.deq.bits.last && returnRQ.io.deq.bits.id === Constants.DATA_ID

    returnRQ.io.enq.valid := io.master.r.fire()
    returnRQ.io.enq.bits  := io.master.r.bits
    returnRQ.io.deq.ready := io.slaves(rSelect).r.ready

    io.slaves
      .map(_.r)
      .foreach(r => {
        r.bits  := returnRQ.io.deq.bits
        r.valid := false.B
      })
    io.slaves(rSelect).r.valid := returnRQ.io.deq.valid
    io.master.r.ready          := returnRQ.io.enq.ready
  }

  //===--------------------------------------------------------------===
  // WriteChannel
  //===--------------------------------------------------------------===
  {
    val awChosen = chosen(io.slaves.map(_.aw.valid))

    val sAWIdle :: sAWWWaiting :: sAWWaiting :: sWWaiting :: Nil = Enum(4)

    val awState        = RegInit(sAWIdle)
    val awSelectLocked = RegInit(0.U(log2Ceil(sCount).W))

    val wFinish = io.master.w.fire() && io.master.w.bits.last

    switch(awState) {
      is(sAWIdle) {
        when(io.slaves(awChosen).aw.valid) {
          awState := Mux(
            io.master.aw.ready && wFinish,
            sAWIdle,
            Mux(
              io.master.aw.ready,
              sWWaiting,
              Mux(
                wFinish,
                sAWWaiting,
                sAWWWaiting
              )
            )
          )
          awSelectLocked := awChosen
        }
      }
      is(sAWWWaiting) {
        when(io.master.aw.ready && wFinish) {
          awState := sAWIdle
        }.elsewhen(io.master.aw.ready) {
            awState := sWWaiting
          }
          .elsewhen(wFinish) {
            awState := sAWWaiting
          }
      }
      is(sAWWaiting) {
        when(io.master.aw.ready) {
          awState := sAWIdle
        }
      }
      is(sWWaiting) {
        when(wFinish) {
          awState := sAWIdle
        }
      }
    }
    val awSelect = Mux(awState === sAWIdle, awChosen, awSelectLocked)

    io.slaves
      .map(_.aw)
      .foreach(aw => {
        aw.ready := false.B
      })
    io.slaves(awSelect).aw.ready := (awState =/= sWWaiting) && io.master.aw.ready
    io.master.aw.bits            := io.slaves(awSelect).aw.bits
    io.master.aw.valid           := (awState =/= sWWaiting) && io.slaves(awSelect).aw.valid

    io.slaves
      .map(_.w)
      .foreach(w => {
        w.ready := false.B
      })
    io.slaves(awSelect).w.ready := (awState =/= sAWWaiting) && io.master.w.ready
    io.master.w.bits            := io.slaves(awSelect).w.bits
    io.master.w.valid           := (awState =/= sAWWaiting) && io.slaves(awSelect).w.valid

    val dataAWQ    = Module(new Queue(UInt(log2Ceil(sCount).W), queueDepth))
    val responseBQ = Module(new Queue(new AXIDataWriteRespBundle, queueDepth, flow = true))

    val bSelect = dataAWQ.io.deq.bits

    dataAWQ.io.enq.valid := io.master.aw.fire()
    dataAWQ.io.enq.bits  := awSelect
    dataAWQ.io.deq.ready := responseBQ.io.deq.fire()

    responseBQ.io.enq.valid := io.master.b.fire()
    responseBQ.io.enq.bits  := io.master.b.bits
    responseBQ.io.deq.ready := io.slaves(bSelect).b.ready

    io.slaves
      .map(_.b)
      .foreach(b => {
        b.bits  := responseBQ.io.deq.bits
        b.valid := false.B
      })
    io.slaves(bSelect).b.valid := responseBQ.io.deq.valid
    io.master.b.ready          := responseBQ.io.enq.ready
  }

}
