import Deps._

name := "featury"

lazy val featuryVersion = "0.3.0-M13-SNAPSHOT"

version := featuryVersion

lazy val shared = Seq(
  scalaVersion := "2.13.8",
  crossScalaVersions := Seq("2.12.15", "2.13.8"),
  version := featuryVersion,
  organization := "io.findify",
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
  ),
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("3")) {
      Seq("-Xmax-inlines", "128")
    } else {
      Nil
    }
  }
)

lazy val mavenSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
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

lazy val core = (project in file("core")).settings(shared: _*).settings(mavenSettings: _*)

lazy val flink = (project in file("flink"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val examples = (project in file("examples"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .settings(publishArtifact := false)
  .dependsOn(flink % "test->test;compile->compile")

lazy val redis = (project in file("connector/redis"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val root = (project in file("."))
  .aggregate(core, flink, redis, examples)
  .settings(
    name := "Featury",
    publishArtifact := false,
    publish / skip := true
  )
  .settings(shared: _*)

sonatypeProfileName := "io.findify"

usePgpKeyHex("6CFDF5062176DE9FB05578013C45A82BD53DADD4")

ThisBuild / evictionErrorLevel := Level.Info
