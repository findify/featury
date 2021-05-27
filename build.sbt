import Deps._

name := "featury"

version := "0.0.1"

lazy val shared = Seq(
  organization := "io.findify",
  scalaVersion := "2.12.13",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.github.blemale" %% "scaffeine" % "4.0.2"
  ),
  version := "0.0.1"
)

scalaVersion := "2.12.13"

lazy val core = (project in file("core")).settings(shared: _*)

lazy val flink = (project in file("flink"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val api = (project in file("api"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;compile->compile")

sonatypeProfileName := "io.findify"
