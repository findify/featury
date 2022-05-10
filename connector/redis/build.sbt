import Deps._

name := "featury-redis"

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "4.2.3"
)
