# 配置

## 环境配置

- Java 8 或 11
- sbt
- 安装完成后，项目构建过程需要用到maven，一般来说会比较慢，可以选择换源，方法如下
  - 找到用户目录下的 `.sbt/` 文件夹，Windows的一般为 `C:\Users\{}\.sbt\` ,花括号为用户名，linux的一般为 `~/.sbt/`
  - 在该 `.sbt/` 文件夹下新建文件 `repositories`
  - 编辑该文件，加入下面的内容

    ```config
    [repositories]
        local
        huaweicloud-maven: https://repo.huaweicloud.com/repository/maven/
        maven-central: https://repo1.maven.org/maven2/
        huaweicloud-ivy: https://repo.huaweicloud.com/repository/ivy/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
        sbt-plugin-repo: https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]
    ```

## IntelliJ IDEA

- Scala插件
- 导入项目：
  - **导入时的目录选择为`Phoenix/chisel/`**
  - 选择`Import project from external model`->`sbt`
  - 一路`next`
  - 然后应该会自动导入依赖，等一会即可