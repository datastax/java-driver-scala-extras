name := "driver-scala-codecs"
version := "1.0"
organizationName := "DataStax"
startYear := Some(2017)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion := "2.12.12"

libraryDependencies ++= Seq(
  "com.datastax.oss" % "java-driver-core" % "4.9.0"  % "provided",
  "org.scalatest"   %% "scalatest"        % "3.2.3"  % "test",
  "org.scalacheck"  %% "scalacheck"       % "1.15.1" % "test"
)

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Ywarn-infer-any"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
