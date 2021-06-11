import Deps._

name := "featury-flink"

lazy val flinkVersion = "1.13.0"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion % "provided",
  "org.apache.flink"  % "flink-connector-files" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-test-utils"      % flinkVersion % "provided, test",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "com.github.luben"  % "zstd-jni"              % "1.4.9-5"
)