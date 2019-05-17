lazy val baseName         = "Topology"
lazy val baseNameL        = baseName.toLowerCase

lazy val projectVersion   = "1.1.2"
lazy val mimaVersion      = "1.1.0"

lazy val deps = new {
  val test = new {
    val scalaTest = "3.1.0-SNAP9"
  }
}

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := baseName,
    // Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
    // and  a `src/main/scala-2.13-` source directory for Scala version older than 2.13
    unmanagedSourceDirectories in Compile += {
      val sourceDir = (sourceDirectory in Compile).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    },
    // ---- console ----
    initialCommands in console :=
    """import de.sciss.topology._
      |import scala.util.{Try, Success, Failure}
      |""".stripMargin,
    mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion)
  )

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  scalaVersion       := "2.12.8",
  crossScalaVersions := Seq("2.12.8", "2.11.12", "2.13.0-RC2"),
  description        := "A dynamic directed acyclic graph library",
  homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}")),
  licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"),
  libraryDependencies += {
    if (scalaVersion.value == "2.13.0-RC2") {
      "org.scalatest" % "scalatest_2.13.0-RC1" % deps.test.scalaTest % Test
    } else {
      "org.scalatest" %% "scalatest" % deps.test.scalaTest % Test
    }
  }
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
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
)

