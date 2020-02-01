package cpu.common {

  import chisel3._


  trait MIPSConstants {
    // opcode in a instruction is up to 31 bit and down to 26 bit
    val OPCODE_MSB = 31
    val OPCODE_LSB = 26

    // R, I type rs register
    val RS_MSB = 25
    val RS_LSB = 21

    // R, I type rt register
    val RT_MSB = 20
    val RT_LSB = 16

    // R type rd register
    val RD_MSB = 15
    val RD_LSB = 11

    // R type shamt
    val SHAMT_MSB = 10
    val SHAMT_LSB = 6

    // R type funct
    val FUNCT_MSB = 5
    val FUNCT_LSB = 0

    // address in J-type and immediate in I-type are note specified
  }



}