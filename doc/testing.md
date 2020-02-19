Documentation for Testing
=======================

now, for the testing part

## automated testing
in this section, we will test the CPU design with tests automatically. The tests 
will be run within intellij

###related files
[CPUTestDriver](src/main/scala/cpu/testing/CPUTestDriver.scala)<br/>
implements the functions and modules for testing<br>
[Test Cases](src/main/scala/cpu/testing/TestCases.scala)<br>
this is the file that contains the testcases, the format are specified 
as in the case class **CPUTestCase** in the CPUTestDriver<br>
[Test Runner](src/test/scala/cpu/CPUTester.scala)<br>
this is the test runner that **run** the test

### things to modify
### naming of variables
this test use treadle as the backend, and as such, it will generate a symbol
table that will be mapped to the actual circuit. You need to find the names for
all the symbols. There are naming conventions you can use to quickly determine
the names. For example, I want to elaborate Top.scala, and the actual pipelined 
CPU is named as cpu inside it. Then to the register file is called **cpu.regFile**.
The name **cpu** comes from the **naming** in top.scala, and the name **regFile**
comes from my actual pipelined cpu, in which I named the val that refers to the 
register file as regfile
### instructions for testing
the instructions should be put in a **subdirectory** inside testMemFile
### instruction format
[mars](https://courses.missouristate.edu/KenVollmar/MARS/)<br>
the instructions should be hex encoded. A way to do this is to invoke the
following command in the command line 
```shell script
java -jar Mars4_5.jar mc CompactTextAtZero a dump .text HexText branch.txt branch.asm
```
the branch.txt is the **output**, the branch.asm is the **source file**. You should modify
the source file and use the script to convert that to hex format and store it in the output file
### name of tests
name of tests is determined by directoryname + "/" + hexfile name

## single stepping
only modifying hardware node names are required. 
**NOTE: to hide garbage output, there are lines that hides the output inside
a wire or a module. Remove or modify them if they get in the way**
### command to run
run pipelined branch branch.txt<br>
pipelined is the CPU model, branch is the subdir under testMemFile, branch.txt is the
hex file to load as memory
