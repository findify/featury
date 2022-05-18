import Deps._

name := "featury-core"

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"               % scalatestVersion % Test,
  "org.scalactic"          %% "scalactic"               % scalatestVersion % Test,
  "org.scalatestplus"      %% "scalacheck-1-16"         % "3.2.12.0"       % Test,
  "ch.qos.logback"          % "logback-classic"         % logbackVersion,
  "com.github.pathikrit"   %% "better-files"            % "3.9.1" withCrossVersion (CrossVersion.for3Use2_13),
  "com.github.scopt"       %% "scopt"                   % "4.0.1",
  "com.google.guava"        % "guava"                   % "30.1.1-jre",
  "com.thesamet.scalapb"   %% "scalapb-runtime"         % scalapbVersion   % "protobuf",
  "io.circe"               %% "circe-core"              % circeVersion,
  "io.circe"               %% "circe-generic"           % circeVersion,
  "io.circe"               %% "circe-parser"            % circeVersion,
  "org.typelevel"          %% "cats-effect"             % "3.3.11",
  "com.github.pureconfig"  %% "pureconfig-core"         % "0.17.1",
  "org.typelevel"          %% "log4cats-core"           % log4catsVersion,
  "org.typelevel"          %% "log4cats-slf4j"          % log4catsVersion,
  "com.github.blemale"     %% "scaffeine"               % "5.2.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.7.0"
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)
