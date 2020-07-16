package uart
/*------------------------------------------------------------------------------
--------------------------------------------------------------------------------
Copyright (c) 2016, Loongson Technology Corporation Limited.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of Loongson Technology Corporation Limited nor the names of
its contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL LOONGSON TECHNOLOGY CORPORATION LIMITED BE LIABLE
TO ANY PARTY FOR DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--------------------------------------------------------------------------------
------------------------------------------------------------------------------*/
import chisel3._
object Constants {
  val UART_ADDR_WIDTH: Int =  3
  val UART_DATA_WIDTH: Int =  8

  // Register addresses
  val UART_REG_RB: UInt = 	 0.U(UART_ADDR_WIDTH.W)	// receiver buffer
  val UART_REG_TR: UInt =   0.U(UART_ADDR_WIDTH.W)	// transmitter
  val UART_REG_IE: UInt = 	 1.U(UART_ADDR_WIDTH.W)	// Interrupt enable
  val UART_REG_II: UInt =   2.U(UART_ADDR_WIDTH.W)	// Interrupt identification
  val UART_REG_FC: UInt =   2.U(UART_ADDR_WIDTH.W)	// FIFO control
  val UART_REG_LC: UInt = 	 3.U(UART_ADDR_WIDTH.W)	// Line Control
  val UART_REG_MC: UInt = 	 4.U(UART_ADDR_WIDTH.W)	// Modem control
  val UART_REG_LS: UInt =   5.U(UART_ADDR_WIDTH.W)	// Line status
  val UART_REG_MS: UInt =   6.U(UART_ADDR_WIDTH.W)	// Modem status
  val UART_REG_SR: UInt =   7.U(UART_ADDR_WIDTH.W)	// Scratch register
  val UART_REG_DL1: UInt =  0.U(UART_ADDR_WIDTH.W)	// Divisor latch bytes (1-2)
  val UART_REG_DL2: UInt =  1.U(UART_ADDR_WIDTH.W)

  // Interrupt Enable register bits
  val UART_IE_RDA: Int = 	  0	// Received Data available interrupt
  val UART_IE_THRE: Int =   1	// Transmitter Holding Register empty interrupt
  val UART_IE_RLS: Int = 	  2	// Receiver Line Status Interrupt
  val UART_IE_MS	: Int =   3	// Modem Status Interrupt

  // Interrupt Identification register bits
  val UART_II_IP: Int = 	0	// Interrupt pending when 0
  `define UART_II_II	3:1	// Interrupt identification

  // Interrupt identification values for bits 3:1
  val UART_II_RLS	 : UInt =  ("b011").U(3.W)	// Receiver Line Status
  val UART_II_RDA	 : UInt =  ("b010").U(3.W)	// Receiver Data available
  val UART_II_TI	 : UInt =  ("b110").U(3.W)	// Timeout Indication
  val UART_II_THRE : UInt =  ("b001").U(3.W)	// Transmitter Holding Register empty
  val UART_II_MS	 : UInt =  ("b000").U(3.W)	// Modem Status

  // FIFO Control Register bits
  `define UART_FC_TL	1:0	// Trigger level

  // FIFO trigger level values
  val UART_FC_1  : UInt = "b00".U(2.W)
  val UART_FC_4  : UInt = "b01".U(2.W)
  val UART_FC_8  : UInt = "b10".U(2.W)
  val UART_FC_14 : UInt = "b11".U(2.W)

  // Line Control register bits
  val UART_LC_BITS  1:0	// bits in character
  val UART_LC_SB	  : Int =2	// stop bits
  val UART_LC_PE	  : Int =3	// parity enable
  val UART_LC_EP	  : Int =4	// even parity
  val  UART_LC_SP	  : Int =5	// stick parity
  val UART_LC_BC	  : Int =6	// Break control
  val UART_LC_DL	  : Int =7	// Divisor Latch access bit
  // Modem Control register bits
  val UART_MC_DTR	  : Int = 0
  val UART_MC_RTS	  : Int = 1
  val UART_MC_OUT1  : Int = 2
  val UART_MC_OUT2  : Int = 3
  val UART_MC_LB	  : Int = 4	// Loopback mode
  // Line Status Register bits
  val UART_LS_DR	: Int = 0	// Data ready
  val UART_LS_OE	: Int = 1	// Overrun Error
  val UART_LS_PE	: Int = 2	// Parity Error
  val UART_LS_FE	: Int = 3	// Framing Error
  val UART_LS_BI	: Int = 4	// Break interrupt
  val UART_LS_TFE	: Int = 5	// Transmit FIFO is empty
  val UART_LS_TE	: Int = 6	// Transmitter Empty indicator
  val UART_LS_EI	: Int = 7	// Error indicator
  // Modem Status Register bits
  val UART_MS_DCTS	: Int = 0	// Delta signals
  val UART_MS_DDSR	: Int = 1
  val UART_MS_TERI	: Int = 2
  val UART_MS_DDCD	: Int = 3
  val UART_MS_CCTS	: Int = 4	// Complement signals
  val UART_MS_CDSR	: Int = 5
  val UART_MS_CRI   : Int = 6
  val UART_MS_CDCD	: Int = 7
  // FIFO parameter defines
  val UART_FIFO_WIDTH	     : Int = 8
  val UART_FIFO_DEPTH	     : Int = 16
  val UART_FIFO_POINTER_W	 : Int = 4
  val UART_FIFO_COUNTER_W	 : Int = 5
  val UART_FIFO_REC_WIDTH  : Int = 11
}
