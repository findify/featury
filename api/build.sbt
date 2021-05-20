import Deps._

name := "featury-api"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect"         % "3.1.0",
  "org.http4s"    %% "http4s-dsl"          % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"    %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"    %% "http4s-circe"        % http4sVersion,
  "org.typelevel" %% "log4cats-core"       % log4catsVersion,
  "org.typelevel" %% "log4cats-slf4j"      % log4catsVersion
)
