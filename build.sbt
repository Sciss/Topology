lazy val baseName         = "Topology"
lazy val baseNameL        = baseName.toLowerCase

lazy val projectVersion   = "1.1.1-SNAPSHOT"
lazy val mimaVersion      = "1.1.0"

lazy val deps = new {
  val test = new {
    val scalaTest = "3.0.5"
  }
}

name               := baseName
version            := projectVersion
organization       := "de.sciss"
scalaVersion       := "2.13.0-M5"
crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-M5")
description        := "A dynamic directed acyclic graph library"
homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}"))
licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint")

libraryDependencies += {
  val v = if (scalaVersion.value == "2.13.0-M5") "3.0.6-SNAP5" else deps.test.scalaTest
  "org.scalatest" %% "scalatest" % v % Test
}

// ---- console ----

initialCommands in console :=
"""import de.sciss.topology._
  |import scala.util.{Try, Success, Failure}
  |""".stripMargin

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@git.iem.at:sciss/{n}.git</url>
  <connection>scm:git:git@git.iem.at:sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}

