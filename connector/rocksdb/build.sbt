import Deps._

name := "featury-rocksdb"

libraryDependencies ++= Seq(
  "com.ververica" % "frocksdbjni" % "6.20.3-ververica-1.0"
)
