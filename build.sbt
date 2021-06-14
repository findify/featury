import Deps._

name := "featury"

version := "0.0.1-SNAPSHOT"

lazy val shared = Seq(
  organization := "io.findify",
  scalaVersion := "2.12.13",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.github.blemale" %% "scaffeine" % "4.0.2"
  ),
  version := "0.0.1-SNAPSHOT",
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value,
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/findify/featury")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/findify/featury"),
      "scm:git@github.com:findify/featury.git"
    )
  ),
  developers := List(
    Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = url("https://dfdx.me/"))
  )
)

scalaVersion := "2.12.13"

lazy val core = (project in file("core")).settings(shared: _*)

lazy val flink = (project in file("flink"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val api = (project in file("api"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;compile->compile")
  .dependsOn(redis % "test->test;compile->compile")

lazy val redis = (project in file("connector/redis"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;compile->compile")

sonatypeProfileName := "io.findify"
