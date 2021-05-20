import Deps._

name := "featury-flink"

lazy val flinkVersion = "1.13.0"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
)
