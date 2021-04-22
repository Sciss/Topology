lazy val baseName         = "Topology"
lazy val baseNameL        = baseName.toLowerCase

lazy val projectVersion   = "1.1.4"
lazy val mimaVersion      = "1.1.0"

lazy val deps = new {
  val test = new {
    val scalaTest = "3.2.8"
  }
}

// sonatype plugin requires that these are in global
ThisBuild / version      := projectVersion
ThisBuild / organization := "de.sciss"

lazy val root = crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(commonSettings)
  .jvmSettings(commonJvmSettings)
  .settings(publishSettings)
  .settings(
    name := baseName,
    // Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
    // and  a `src/main/scala-2.13-` source directory for Scala version older than 2.13
    unmanagedSourceDirectories in Compile ++= {
      val sourceDirPl = (sourceDirectory in Compile).value
      val sourceDirSh = file(
        sourceDirPl.getPath.replace("/jvm/" , "/shared/").replace("/js/", "/shared/")
      )
      val sv = CrossVersion.partialVersion(scalaVersion.value)
      val sub = sv match {
        case Some((2, n)) if n >= 13 => "scala-2.13+"
        case Some((3, _))            => "scala-2.13+"
        case _                       => "scala-2.13-"
      }
      Seq(sourceDirPl / sub, sourceDirSh / sub)
    },
    // ---- console ----
    initialCommands in console :=
    """import de.sciss.topology._
      |import scala.util.{Try, Success, Failure}
      |""".stripMargin,
    mimaPreviousArtifacts := Set("de.sciss" %% baseNameL % mimaVersion)
  )

lazy val commonJvmSettings = Seq(
  crossScalaVersions := Seq("3.0.0-RC3", "2.13.5", "2.12.13"),
)

lazy val commonSettings = Seq(
//  version            := projectVersion,
//  organization       := "de.sciss",
  scalaVersion       := "2.13.5",
  description        := "A dynamic directed acyclic graph library",
  homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}")),
  licenses           := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"),
  libraryDependencies += {
    "org.scalatest" %%% "scalatest" % deps.test.scalaTest % Test
  }
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  developers := List(
    Developer(
      id    = "sciss",
      name  = "Hanns Holger Rutz",
      email = "contact@sciss.de",
      url   = url("https://www.sciss.de")
    )
  ),
  scmInfo := {
    val h = "git.iem.at"
    val a = s"sciss/${name.value}"
    Some(ScmInfo(url(s"https://$h/$a"), s"scm:git@$h:$a.git"))
  },
)

