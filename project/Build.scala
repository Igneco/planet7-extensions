import sbt._
import Keys._

object Build extends Build {

  def sharedSettings = Seq(
    scalaVersion:= "2.11.5",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.1.0-RC1",
      "com.github.agmenc" %% "planet7" % "0.1.7",
      "junit" % "junit" % "4.11" % "test",
      "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    ),
    // add scala-xml dependency when needed (for Scala 2.11 and newer)
    // this mechanism supports cross-version publishing
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 => libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
        case _ => libraryDependencies.value
      }
    }
  )

  lazy val main = Project(id = "shapeful", base = file(".")).settings(sharedSettings: _*)
}
