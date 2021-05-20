import Deps._

name := "featury-cassandra"

libraryDependencies ++= Seq(
  "com.datastax.oss" % "java-driver-core" % cassandraDriverVersion
)
