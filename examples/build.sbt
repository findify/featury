import Deps._

name := "featury-examples"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion,
  "org.apache.flink" %% "flink-clients"         % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink"  % "flink-connector-files" % flinkVersion
)
