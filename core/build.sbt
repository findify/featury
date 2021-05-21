import Deps._

name := "featury-core"

libraryDependencies ++= Seq(
  "org.scalatest"        %% "scalatest"       % scalatestVersion                        % Test,
  "org.scalactic"        %% "scalactic"       % scalatestVersion                        % Test,
  "org.scalatestplus"    %% "scalacheck-1-14" % "3.2.2.0"                               % Test,
  "ch.qos.logback"        % "logback-classic" % "1.2.3",
  "com.github.pathikrit" %% "better-files"    % "3.9.1",
  "com.github.scopt"     %% "scopt"           % "4.0.1",
  "com.google.guava"      % "guava"           % "30.1.1-jre",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)


Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)
