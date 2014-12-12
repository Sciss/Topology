name               := "Topology"

version            := "1.0.0-SNAPSHOT"

organization       := "de.sciss"

scalaVersion       := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4")

description        := "A dynamic directed acyclic graph library"

homepage           := Some(url("https://github.com/Sciss/" + name.value))

licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

//lazy val scalaTestVersion = "2.2.2"
//
//libraryDependencies ++= Seq(
//  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
//)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

// ---- console ----

initialCommands in console :=
"""import de.sciss.topology._
  |import scala.util.{Try, Success, Failure}
  |case class Vertex(label: String)
  |case class Edge(sourceVertex: Vertex, targetVertex: Vertex) extends Topology.Edge[Vertex]
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

