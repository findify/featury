name := "featury"

version := "0.1"

scalaVersion := "2.12.12"

lazy val http4sVersion          = "1.0.0-M21"
lazy val log4catsVersion        = "2.1.0"
lazy val scalatestVersion       = "3.2.8"
lazy val circeVersion           = "0.13.0"
lazy val circeYamlVersion       = "0.13.1"
lazy val cassandraDriverVersion = "4.11.1"

libraryDependencies ++= Seq(
  "org.typelevel"        %% "cats-effect"               % "3.1.0",
  "com.github.blemale"   %% "scaffeine"                 % "4.0.2",
  "io.circe"             %% "circe-yaml"                % circeYamlVersion,
  "io.circe"             %% "circe-core"                % circeVersion,
  "io.circe"             %% "circe-generic"             % circeVersion,
  "io.circe"             %% "circe-generic-extras"      % circeVersion,
  "io.circe"             %% "circe-parser"              % circeVersion,
  "org.http4s"           %% "http4s-dsl"                % http4sVersion,
  "org.http4s"           %% "http4s-blaze-server"       % http4sVersion,
  "org.http4s"           %% "http4s-blaze-client"       % http4sVersion,
  "org.http4s"           %% "http4s-circe"              % http4sVersion,
  "org.typelevel"        %% "log4cats-core"             % log4catsVersion,
  "org.typelevel"        %% "log4cats-slf4j"            % log4catsVersion,
  "org.scalatest"        %% "scalatest"                 % scalatestVersion % Test,
  "org.scalactic"        %% "scalactic"                 % scalatestVersion % Test,
  "org.scalatestplus"    %% "scalacheck-1-14"           % "3.2.2.0"        % Test,
  "ch.qos.logback"        % "logback-classic"           % "1.2.3",
  "com.github.pathikrit" %% "better-files"              % "3.9.1",
  "com.github.scopt"     %% "scopt"                     % "4.0.1",
  "com.github.fppt"       % "jedis-mock"                % "0.1.19"         % Test,
  "redis.clients"         % "jedis"                     % "3.6.0",
  "com.google.guava"      % "guava"                     % "30.1.1-jre",
  "com.datastax.oss"      % "java-driver-core"          % cassandraDriverVersion,
  "com.datastax.oss"      % "java-driver-query-builder" % cassandraDriverVersion
)
