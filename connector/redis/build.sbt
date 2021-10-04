import Deps._

name := "featury-redis"

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "3.7.0"
)
