import Deps._

name := "featury-examples"

libraryDependencies ++= Seq(
  "org.apache.flink" % "flink-clients"         % flinkVersion,
  "org.apache.flink" % "flink-connector-files" % flinkVersion
)

libraryDependencies ++= {
  if (scalaBinaryVersion.value.startsWith("2")) {
    Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
    )
  } else {
    Seq(
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % Provided
    )
  }
}
