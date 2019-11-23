name := "driver-scala-codecs"
version := "1.1"
organizationName := "DataStax"
startYear := Some(2017)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0",
  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.3.0" % "optional",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" % "scalacheck_2.12" % "1.13.5" % "test"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation", "-unchecked", "-feature", "-Xlint", "-Ywarn-infer-any")

javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

