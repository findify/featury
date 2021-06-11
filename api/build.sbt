import Deps._

name := "featury-api"

libraryDependencies ++= Seq(
  "org.http4s"       %% "http4s-dsl"          % http4sVersion,
  "org.http4s"       %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"       %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"       %% "http4s-circe"        % http4sVersion,
  "org.typelevel"    %% "log4cats-core"       % log4catsVersion,
  "org.typelevel"    %% "log4cats-slf4j"      % log4catsVersion,
  "redis.clients"     % "jedis"               % "3.6.0",
  "com.github.scopt" %% "scopt"               % "4.0.1"
)

scalacOptions += "-Ypartial-unification"
