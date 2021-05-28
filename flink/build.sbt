import Deps._

name := "featury-flink"

lazy val flinkVersion = "1.13.0"

libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala"           % flinkVersion % "provided",
  "org.apache.flink"  % "flink-connector-files" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-test-utils"      % flinkVersion % "provided, test",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "com.github.luben"  % "zstd-jni"              % "1.4.9-5",
  "io.findify"       %% "flink-adt"             % "0.4.0-M3"
)

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/findify/featury"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/findify/featury"),
    "scm:git@github.com:findify/featury.git"
  )
)
developers := List(
  Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = url("https://dfdx.me/"))
)
