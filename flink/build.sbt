import Deps._
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

name := "featury-flink"

lazy val flinkVersion = "1.13.2"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion % "provided",
  "org.apache.flink"  % "flink-connector-files" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-test-utils"      % flinkVersion % "provided, test",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "com.github.luben"  % "zstd-jni"              % "1.5.0-2",
  "io.findify"       %% "flink-adt"             % "0.4.2"
)
