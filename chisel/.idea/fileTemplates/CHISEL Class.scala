// See README.md for license details.

#if ((${PACKAGE_NAME} && ${PACKAGE_NAME} != ""))package ${PACKAGE_NAME} #end
#parse("File Header.java")

import chisel3._

class ${NAME} extends Module {
    val io = IO(new Bundle {
    
    })
}
