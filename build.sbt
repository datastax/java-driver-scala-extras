lazy val scala213               = "2.13.4"
lazy val scala212               = "2.12.12"
lazy val supportedScalaVersions = List(scala213, scala212)

lazy val extras = project
  .in(file("."))
  .settings(
    name := "driver-scala-codecs",
    version := "1.0",
    organizationName := "DataStax",
    startYear := Some(2017),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion := scala213,
    crossScalaVersions := supportedScalaVersions,
// FIXME doing `+test` causes CassandraUnit throw 'java.lang.RuntimeException: javax.management.InstanceAlreadyExistsException: org.apache.cassandra.db:type=DynamicEndpointSnitch'
// Tested individually works fine
    libraryDependencies ++= Seq(
      "com.datastax.oss"  % "java-driver-core" % "4.9.0"   % "provided",
      "org.scalatest"    %% "scalatest"        % "3.2.3"   % "test",
      "org.scalacheck"   %% "scalacheck"       % "1.15.1"  % "test",
      "org.cassandraunit" % "cassandra-unit"   % "4.3.1.0" % "test"
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-Xlint"
    ),
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    testOptions in Test += Tests.Argument("-oF") // Show full stack trace
  )

lazy val bench = project
  .settings(
    scalaVersion := "2.13.4",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.datastax.oss"  % "java-driver-core" % "4.9.0" % "test",
      "com.storm-enroute" %% "scalameter" % "0.20" % "test"
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    parallelExecution in Test := false,
    logBuffered := false
  )
  .dependsOn(extras)
