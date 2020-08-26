# amadeus-mips • [![action state](https://github.com/amadeus-mips/amadeus-mips/workflows/func-test/badge.svg)](https://github.com/amadeus-mips/amadeus-mips/actions)

## Setup

**注意！！IntelliJ IDEA 导入的目录是 `Phoenix/chisel/`** 配置详见 [Setup](doc/Setup.md)。

## 目录结构

`chisel/` 为chisel项目文件夹

`tests/` 包含一些测试资源

### 仿真

功能和性能测试顶层模块为 `soc.SocLiteTop`， 系统测试顶层模块为 `soc.SocUpTop`，位于 `chisel/src/main/scala/soc/`

仿真 testbench 位于 `chisel/src/test/scala/soc`

#### Example

运行功能测试（于 `chisel` 目录下）

```sh
$ sbt 'testOnly soc.FuncWithVcdTest'
```

更多测试与配置见 `soc.*Test`

### CPU 生成

CPU 模块位于 `cpu.CPUTop`， 生成方法（于 `chisel` 目录下）

```sh
$ sbt 'runMain cpu.elaborateCPU'
```

生成后的 CPU 位于 `chisel/generation` 中，由于使用 `BlackBox` 同时附带 `divider.v`。

若要用于龙芯杯的各种测试，需要附带 `chisel/src/main/resources/mycpu_top.v` 封装 CPUTop。

---

## License

see [LICENSE](LICENSE)
