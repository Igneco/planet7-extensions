import sbt._
import Keys._

object Build extends Build {

  def sharedSettings = Seq(
    scalaVersion:= "2.12.0",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),
    crossScalaVersions := Seq("2.11.7", "2.12.0"),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.2",
      "com.github.agmenc" %% "planet7" % "0.1.15",
      "junit" % "junit" % "4.11" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  )

  lazy val main = Project(id = "planet7-extensions", base = file(".")).settings(sharedSettings: _*)
}
