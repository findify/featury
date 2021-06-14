import Deps._

name := "featury-redis"

libraryDependencies ++= Seq(
  "org.typelevel" %% "log4cats-core"  % log4catsVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
  "redis.clients"  % "jedis"          % "3.6.0"
)

scalacOptions += "-Ypartial-unification"
