val scalaMajorVersion = SettingKey[Int]("scalaMajorVersion")

// copied from Roman Janusz's Silencer plugin (https://github.com/ghik/silencer/)
val saveTestClasspath = taskKey[File](
  "Saves test classpath to a file so that it can be used by embedded scalac in tests")

lazy val root = (project in file("."))
  .settings(defaults)
  .settings(
    name := "genjavadoc-plugin",
    scalaMajorVersion := CrossVersion.partialVersion(scalaVersion.value).get._2.toInt,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "junit" % "junit" % "4.13.2" % Test,
      "com.github.sbt" % "junit-interface" % "0.13.3" % Test
    ),
    // make JUnit more verbose (info print instead of debug, w/ exception names & stacktraces)
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v")),
    saveTestClasspath := {
      val result = (Test / classDirectory).value / "embeddedcp"
      IO.write(result, (Test / fullClasspath).value.map(_.data.getAbsolutePath).mkString("\n"))
      result
    },
    Test / test := {
      saveTestClasspath.value
      (Test / test).value
    },
    Test / fork := true,
    Compile / unmanagedSourceDirectories := {
      val default = (Compile / unmanagedSourceDirectories).value
      def r(from: String, to: String) = default.map(f => new java.io.File(f.getPath.replaceAll(from, to)))
      if (scalaVersion.value == "2.12.0") r("""/scala-2\.12$""", "/scala-2.11")
      else if (scalaMajorVersion.value == 13) r("""/scala-2\.13[^/]*$""", "/scala-2.13")
      else default
    },
    crossVersion := CrossVersion.full,
    exportJars := true,
    scalacOptions ++=
      Seq("-deprecation", "-feature", "-unchecked", "-Xlint") ++ (
        if (scalaMajorVersion.value == 13) Seq() // deprecation warnings due to SortedSet.from/to
        else Seq("-Xfatal-warnings"))
  )

lazy val defaults = Seq(
  organization := "com.typesafe.genjavadoc",
  sonatypeProfileName := "com.typesafe",
  scalaVersion := crossScalaVersions.value.last,
  crossScalaVersions := {
    val supportedScala212Versions = (5 to 18).map(p => s"2.12.$p")
    val supportedScala213Versions = (0 to 10).map(p => s"2.13.$p")
    supportedScala212Versions ++ supportedScala213Versions
  },
  scalaMajorVersion := CrossVersion.partialVersion(scalaVersion.value).get._2.toInt,
  resolvers += Resolver.mavenLocal,
  startYear := Some(2012),
  homepage := Some(url("https://github.com/lightbend/genjavadoc")),
  licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
  scmInfo := Some(ScmInfo(url("https://github.com/lightbend/genjavadoc"), "git@github.com:lightbend/genjavadoc.git")),
  developers += Developer("contributors",
                          "Contributors",
                          "https://gitter.im/akka/dev",
                          url("https://github.com/lightbend/genjavadoc/graphs/contributors"))
)

lazy val browse = SettingKey[Boolean]("browse", "run with -Ybrowse:uncurry")
