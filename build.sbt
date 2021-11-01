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
      "com.novocode" % "junit-interface" % "0.11" % Test
    ),
    // make JUnit more verbose (info print instead of debug, w/ exception names & stacktraces)
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v")),
    saveTestClasspath := {
      val result = (classDirectory in Test).value / "embeddedcp"
      IO.write(result, (fullClasspath in Test).value.map(_.data.getAbsolutePath).mkString("\n"))
      result
    },
    test in Test := {
      saveTestClasspath.value
      (test in Test).value
    },
    fork in Test := true,
    unmanagedSourceDirectories in Compile := {
      val default = (unmanagedSourceDirectories in Compile).value
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
    // Remember to keep this list aligned with the Scala version matrix in .github/workflows/validate.yml
    // 2.11.6 is the first to be supported and we skip 2.11.9 and 2.11.10
    val supportedScala211Versions = Seq("2.11.6", "2.11.7", "2.11.8", "2.11.11", "2.11.12")
    // Scala 2.12.[0-2] are not supported
    val supportedScala212Versions = (3 to 15).map(p => s"2.12.$p")
    val supportedScala213Versions = (0 to 7).map(p => s"2.13.$p")
    supportedScala211Versions ++ supportedScala212Versions ++ supportedScala213Versions
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
