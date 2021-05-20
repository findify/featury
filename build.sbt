import Deps._

name := "featury"

version := "0.1"

lazy val shared = Seq(
  organization := "io.findify",
  scalaVersion := "2.12.12",
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.github.blemale" %% "scaffeine" % "4.0.2"
  )
)

scalaVersion := "2.12.12"

lazy val core = (project in file("core")).settings(shared: _*)

lazy val flink = (project in file("connector/flink"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;it->it;compile->compile")

lazy val spark = (project in file("connector/spark"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;it->it;compile->compile")

lazy val cassandra = (project in file("connector/cassandra"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;it->it;compile->compile")

lazy val redis = (project in file("connector/redis"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;it->it;compile->compile")

lazy val api = (project in file("api"))
  .settings(shared: _*)
  .dependsOn(core % "test->test;it->it;compile->compile")
