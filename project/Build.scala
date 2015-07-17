import sbt._
import Keys._

object Build extends Build {

  def sharedSettings = Seq(
    scalaVersion:= "2.11.7",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),
    crossScalaVersions := Seq("2.11.7"),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.1.0-RC2",
      "com.github.agmenc" %% "planet7" % "0.1.14",
      "junit" % "junit" % "4.11" % "test",
      "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    )
  )

  lazy val main = Project(id = "planet7-extensions", base = file(".")).settings(sharedSettings: _*)
}
