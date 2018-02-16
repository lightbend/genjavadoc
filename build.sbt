// so we can set this from automated builds and also depending on Scala version
lazy val scalaTestVersion = settingKey[String]("The version of ScalaTest to use.")

// copied from Roman Janusz's Silencer plugin (https://github.com/ghik/silencer/)
val saveTestClasspath = taskKey[File](
  "Saves test classpath to a file so that it can be used by embedded scalac in tests")

lazy val genjavadoc = (project in file("."))
  .aggregate(`genjavadoc-plugin`)
  .settings(defaults)
  .settings(
    publishArtifact := false,
    git.useGitDescribe := true
  )
  .enablePlugins(GitVersioning)

lazy val `genjavadoc-plugin` = (project in file("plugin"))
  .settings(defaults)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % scalaTestVersion.value % "test"
    ),
    saveTestClasspath := {
      val result = (classDirectory in Test).value / "embeddedcp"
      IO.write(result, (fullClasspath in Test).value.map(_.data.getAbsolutePath).mkString("\n"))
      result
    },
    (test in Test) := {
      // since we are building for different Scala patch versions, a clean
      // is required to avoid conflicts in class files
      clean.value
      saveTestClasspath.value
      (test in Test).value
    },
    fork in Test := true,
    unmanagedSourceDirectories in Compile := {
      val default = (unmanagedSourceDirectories in Compile).value
      def r(from: String, to: String) = default.map(f => new java.io.File(f.getPath.replaceAll(from, to)))
      if (scalaVersion.value == "2.12.0") r("""/scala-2\.12$""", "/scala-2.11")
      else if (scalaVersion.value.startsWith("2.13.")) r("""/scala-2\.13[^/]*$""", "/scala-2.12")
      else default
    },
    crossVersion := CrossVersion.full,
    exportJars := true,
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Xfatal-warnings")
  )

lazy val defaults = Seq(
  organization := "com.typesafe.genjavadoc",
  scalaVersion := crossScalaVersions.value.last,
  crossScalaVersions := {
    val latest211 = 12
    val latest212 = 4
    val pre213 = List("M3")
    val skipVersions = Set("2.11.9", "2.11.10")
    val scala211Versions =
      (0 to latest211)
        .map(i => s"2.11.$i")
        .filterNot(skipVersions.contains(_))
    ifJavaVersion(_ < 8) {
      scala211Versions
    } {
      scala211Versions ++ (0 to latest212).map(i => s"2.12.$i") ++ pre213.map(s => s"2.13.0-$s")
    }
  },
  scalaTestVersion := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => "3.0.5-M1"  // only version available for 2.13.0-M3
      case Some((2, 12)) => "3.0.4"
      case _ => "2.1.3"
    }
  },
  resolvers += Resolver.mavenLocal,
  publishTo := {
    if (version.value endsWith "-SNAPSHOT") Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  },
  pomExtra :=
    (<inceptionYear>2012</inceptionYear>
     <url>http://github.com/lightbend/genjavadoc</url>
     <licenses>
       <license>
         <name>Apache 2</name>
         <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
         <distribution>repo</distribution>
       </license>
     </licenses>
     <scm>
       <url>git://github.com/lightbend/genjavadoc.git</url>
       <connection>scm:git:git@github.com:lightbend/genjavadoc.git</connection>
     </scm>) ++ makeDevelopersXml(Map(
      "rkuhn" -> "Roland Kuhn")))

lazy val browse = SettingKey[Boolean]("browse", "run with -Ybrowse:uncurry")

def makeDevelopersXml(users: Map[String, String]) =
  <developers>
    {
      for ((id, user) ← users)
        yield <developer><id>{ id }</id><name>{ user }</name></developer>
    }
  </developers>


def ifJavaVersion[T](predicate: Int => Boolean)(yes: => T)(no: => T): T = {
  System.getProperty("java.version").split("\\.").toList match {
    case "1" :: v :: _ if predicate(v.toInt)  => yes
    case _ => no
  }
}
