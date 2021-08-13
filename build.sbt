import Deps._

name := "featury"

lazy val featuryVersion = "0.1.3-M1-SNAPSHOT"

version := featuryVersion

lazy val shared = Seq(
  scalaVersion := "2.12.14",
  version := featuryVersion,
  organization := "io.findify"
)

lazy val mavenSettings = Seq(
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

scalaVersion := "2.12.14"

lazy val core = (project in file("core")).settings(shared: _*).settings(mavenSettings: _*)

lazy val flink = (project in file("flink"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")
  .dependsOn(rocksdb % "test->test;compile->compile")

lazy val api = (project in file("api"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")
  .dependsOn(redis % "test->test;compile->compile")
  .dependsOn(cassandra % "test->test;compile->compile")

lazy val redis = (project in file("connector/redis"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val cassandra = (project in file("connector/cassandra"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val rocksdb = (project in file("connector/rocksdb"))
  .settings(shared: _*)
  .settings(mavenSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val root = (project in file("."))
  .aggregate(core, flink, api, redis, cassandra, rocksdb)
  .settings(
    name := "Featury"
  )
  .settings(shared: _*)

sonatypeProfileName := "io.findify"

usePgpKeyHex("6CFDF5062176DE9FB05578013C45A82BD53DADD4")
