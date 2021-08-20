import Deps._

name := "featury-examples"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
)
