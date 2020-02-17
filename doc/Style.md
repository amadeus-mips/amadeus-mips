# Style

## 缩写

| Abbreviation | Raw | Explain |
| --- | --- | --- |
| IF | Instruction fetch ||
| ID | instruction decoder||
| EXE, EX | Execute ||
| MEM | Memory ||
| WB | Write back ||
||||
| addr | Address ||
| CP | Coprocessor ||
| CP0 | Coprocessor 0 | System control register file. |
| exc | Exception | Such as address not alignment, signed add overflow |
| in | Input ||
| inst | Instruction ||
| intr | Interrupt ||
| len | Length ||
| op | Operation | Used in Execute and Memory stage to determine how to execute. |
| op1 | operator 1 ||
| op2 | operator 2 ||
| out | Output ||
| reg | Register | Sometimes refer to the registers in register-file. |
| regFile | Register-file ||

## 约定

- Amount 表示数量，多用于Vec结构

    ```scala
    val except = Output(Vec(exceptionTypeAmount, Bool()))
    ```

- Len 表示`Wire`宽度，多用于`Data`类, 如`UInt`

    ```scala
    val rsData = Input(UInt(dataLen.W))
    ```

- Bundle的子类命名
  - 含有`Input`和`Output`，则后缀为IO
  - 否则后缀为Bundle

- 以CPBundle为例，不带数据的命名为Control，带数据的由control和data组成

    ```scala
    class CPBundle extends Bundle {
      val control = new CPControlBundle
      val data = UInt(dataLen.W)
    }

    class CPControlBundle extends Bundle {
      val enable = Bool()
      val address = UInt(regAddrLen.W)
      val sel = UInt(3.W)
    }
    ```

- write, read 默认的对象为寄存器堆(regfile)

- enable表示使能，valid表示有效
- 一般情况下，流水顶层模块io中的in表示从上一级流水阶段来的数据，out表示往下一流水阶段去的数据
