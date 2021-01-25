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
    libraryDependencies ++= Seq(
      "com.datastax.oss"        % "java-driver-core"        % "4.9.0"   % "provided",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.2",
      "org.scalatest"          %% "scalatest"               % "3.2.3"   % "test",
      "org.scalacheck"         %% "scalacheck"              % "1.15.1"  % "test",
      "org.cassandraunit"       % "cassandra-unit"          % "4.3.1.0" % "test",
      "org.mockito"             % "mockito-core"            % "3.7.7"   % "test"
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
    testOptions in Test += Tests.Argument("-oF"), // Show full stack
    // Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
    // and a `src/main/scala-2.13-` source directory for Scala version older than 2.13
    unmanagedSourceDirectories in Compile += {
      val sourceDir = (sourceDirectory in Compile).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _ => sourceDir / "scala-2.13-"
      }
    },

    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Seq()
        case _ => Seq(
          // Only include this on 2.13-, we use `scala.jdk` on 2.13+
          "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"
        )
      }
    }
  )
