// See README.md for license details.

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "phoenix"

version := "3.3.2"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.12.10", "2.11.12")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// enable @chiselName and other macros
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Seq(
  "chisel-iotesters" -> "1.4.1+",
  "chiseltest" -> "0.2.1+"
)

libraryDependencies ++= defaultVersions.map { case (dep, ver) =>
  "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", ver)
}

libraryDependencies += "net.fornwall.jelf" % "jelf" % "0.4.1"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.2.1"

libraryDependencies += "org.typelevel" %% "spire" % "0.14.1"
// https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "5.8.1.202007141445-r"


scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)
