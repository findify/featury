import Deps._
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

name := "featury-flink"

libraryDependencies ++= Seq(
  "org.apache.flink" % "flink-connector-files"      % flinkVersion % "provided",
  "org.apache.flink" % "flink-test-utils"           % flinkVersion % "provided, test",
  "org.apache.flink" % "flink-statebackend-rocksdb" % flinkVersion % "provided",
  "org.apache.flink" % "flink-state-processor-api"  % flinkVersion, // not bundled as it's typically not in dist
  "com.github.luben" % "zstd-jni"                   % "1.5.2-2",
  "io.findify"      %% "flink-scala-api"            % "1.15-1"
)
