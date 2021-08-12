import Deps._

name := "featury-cassandra"

libraryDependencies ++= Seq(
  "com.datastax.oss"       % "java-driver-core"    % "4.12.0",
  "org.cognitor.cassandra" % "cassandra-migration" % "2.4.0_v4"
)
