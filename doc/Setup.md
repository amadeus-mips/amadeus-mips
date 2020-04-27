# 配置推荐

## 通用配置

- JDK1.8(即Java 8)
- 下载sbt--[scala官网链接](https://www.scala-sbt.org/download.html), 版本1.3.2, 若使用其它版本，最终仍会下载1.3.2
- 可以默认安装，也可以自己更改安装路径
- 安装完成后，项目构建过程需要用到maven，一般来说会比较慢，可以选择换源，方法如下
  - 找到用户目录下的 `.sbt/` 文件夹，Windows的一般为 `C:\Users\{}\.sbt\` ,花括号为用户名，linux的一般为 `~/.sbt/`
  - 在该 `.sbt/` 文件夹下新建文件 `repositories`
  - 编辑该文件，加入内容

    ```config
    [repositories]
        local
        huaweicloud-maven: https://repo.huaweicloud.com/repository/maven/
        maven-central: https://repo1.maven.org/maven2/
        huaweicloud-ivy: https://repo.huaweicloud.com/repository/ivy/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
        sbt-plugin-repo: https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
    ```

  - 如果已经换过源可以忽略，不过要注意http的源在sbt下载过程中会报warning，应该没有影响。

编辑器可使用 [IntelliJ IDEA](#intellij-idea) 或 [VSCode](#vscode)

各自优点

- idea的代码提示和格式化可能会更好一点，可以直接在代码中选择运行主类或者运行测试。
- vscode的体积更小，可以使用`live share`进行同步开发，虽然idea也有`saros`实现类似功能，但是体验不如`live share`

### IntelliJ IDEA

- 下载：[官网链接](https://www.jetbrains.com/idea/download)。推荐选择`Ultimate`版，收费，但是学生邮箱认证后免费（一年有效，到期后再认证一次即可），用学校邮箱就行。`Community`版，免费，也可以使用。
- 安装
- 打开以后，安装scala插件：
  - `File`->`Setting`（或者快捷键`Ctrl+Alt+S`), 选择`Plugin`，选择顶部的`Marketplace`标签，然后在下面的搜索栏搜索`scala`, 然后安装。安装过程可能会有点慢。
- **导入项目**：
  - 导入时的目录选择为`.../Phoenix/chisel/`, 点击next
  - 选择`Import project from external model`->`sbt`, 点击next
  - 勾选`for imports`和`for builds`（这一步不确定是否有效，至少我这样配置是可用的），点击finish
  - 然后应该会自动导入依赖，等一会即可

### VSCode

- 下载：[官网链接](https://code.visualstudio.com/Download)。免费开源
- 安装
- 打开以后选择打开文件夹，目录为`.../Pheneix/`或`.../Phoenix/chisel/`都可以
- 点击左侧的插件图标，搜索`scala`, **选择`Scala (Metals)`**，安装完成后会自动开始导入，等一会即可
