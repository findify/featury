import Deps._

name := "featury-core"

libraryDependencies ++= Seq(
  "org.scalatest"         %% "scalatest"            % scalatestVersion % Test,
  "org.scalactic"         %% "scalactic"            % scalatestVersion % Test,
  "org.scalatestplus"     %% "scalacheck-1-14"      % "3.2.2.0"        % Test,
  "ch.qos.logback"         % "logback-classic"      % logbackVersion,
  "com.github.pathikrit"  %% "better-files"         % "3.9.1",
  "com.github.scopt"      %% "scopt"                % "4.0.1",
  "com.google.guava"       % "guava"                % "30.1.1-jre",
  "com.thesamet.scalapb"  %% "scalapb-runtime"      % scalapbVersion   % "protobuf",
  "io.circe"              %% "circe-yaml"           % circeYamlVersion,
  "io.circe"              %% "circe-core"           % circeVersion,
  "io.circe"              %% "circe-generic"        % circeVersion,
  "io.circe"              %% "circe-generic-extras" % circeVersion,
  "io.circe"              %% "circe-parser"         % circeVersion,
  "org.typelevel"         %% "cats-effect"          % "3.2.8",
  "com.github.pureconfig" %% "pureconfig"           % "0.16.0",
  "org.typelevel"         %% "log4cats-core"        % log4catsVersion,
  "org.typelevel"         %% "log4cats-slf4j"       % log4catsVersion,
  "com.github.blemale"    %% "scaffeine"            % "5.1.1"
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)
