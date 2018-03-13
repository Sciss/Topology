lazy val baseName         = "Topology"
lazy val baseNameL        = baseName.toLowerCase

lazy val projectVersion   = "1.1.0"
lazy val mimaVersion      = "1.1.0"

lazy val scalaTestVersion = "3.0.5-M1"

name               := baseName
version            := projectVersion
organization       := "de.sciss"
scalaVersion       := "2.13.0-M3" // "2.12.4"
crossScalaVersions := Seq("2.12.4", "2.11.12")
description        := "A dynamic directed acyclic graph library"
homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))
licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

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
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}

