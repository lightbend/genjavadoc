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
      "junit" % "junit" % "4.13.1" % Test,
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
    val earliest211 = 6
    val latest211 = 12
    val latest212 = 12
    val latest213 = 3
    val skipVersions = Set("2.11.9", "2.11.10")
    val scala211Versions =
      (earliest211 to latest211)
        .map(i => s"2.11.$i")
        .filterNot(skipVersions.contains(_))
    ifJavaVersion(_ < 8) {
      scala211Versions
    } {
      scala211Versions ++ (0 to latest212).map(i => s"2.12.$i") ++ (0 to latest213).map(i => s"2.13.$i")
    }
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

def ifJavaVersion[T](predicate: Int => Boolean)(yes: => T)(no: => T): T = {
  System.getProperty("java.version").split("\\.").toList match {
    case "1" :: v :: _ if predicate(v.toInt)  => yes
    case _ => no
  }
}
