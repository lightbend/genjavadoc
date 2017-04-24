/**
 *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package genjavadoc

import sbt._
import sbt.Keys._
import com.typesafe.sbt.GitVersioning
import com.typesafe.sbt.SbtGit._

object B extends Build {
  import sbt.settingKey

  // so we can set this from automated builds and also depending on Scala version
  lazy val scalaTestVersion = settingKey[String]("The version of ScalaTest to use.")

  // copied from Roman Janusz's Silencer plugin (https://github.com/ghik/silencer/)
  val saveTestClasspath = taskKey[File](
    "Saves test classpath to a file so that it can be used by embedded scalac in tests")

  override lazy val settings = super.settings ++ Seq(
    organization := "com.typesafe.genjavadoc",
    scalaVersion := crossScalaVersions.value.last,
    crossScalaVersions := {
      val latest210 = 6
      val latest211 = 11
      val latest212 = 2
      val skipVersions = Set("2.11.9", "2.11.10")
      val scala210and211Versions = (2 to latest210).map(i => s"2.10.$i") ++ (0 to latest211).map(i => s"2.11.$i")
        .filterNot(skipVersions.contains(_))
      ifJavaVersion(_ < 8) {
        scala210and211Versions
      } {
        scala210and211Versions ++ (0 to latest212).map(i => s"2.12.$i")
      }
    },
    scalaTestVersion := {
      val scalaV = scalaVersion.value
      if (scalaV startsWith "2.12") "3.0.0"
      else "2.1.3"
    },
    resolvers += Resolver.mavenLocal)

  lazy val top = (project in file("."))
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
        if (scalaVersion.value == "2.12.0") default.map(f => new java.io.File(f.getPath.replaceAll("/scala-2.12$", "/scala-2.11")))
        else default
      },
      crossVersion := CrossVersion.full,
      exportJars := true
    )

  lazy val defaults = Seq(
    publishTo <<= (version)(v ⇒
      if (v endsWith "-SNAPSHOT") Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      else Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")),
    pomExtra :=
      (<inceptionYear>2012</inceptionYear>
       <url>http://github.com/typesafehub/genjavadoc</url>
       <licenses>
         <license>
           <name>Apache 2</name>
           <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
           <distribution>repo</distribution>
         </license>
       </licenses>
       <scm>
         <url>git://github.com/typesafehub/genjavadoc.git</url>
         <connection>scm:git:git@github.com:typesafehub/genjavadoc.git</connection>
       </scm>) ++ makeDevelopersXml(Map(
        "rkuhn" -> "Roland Kuhn")))

  lazy val browse = SettingKey[Boolean]("browse", "run with -Ybrowse:uncurry")

  private[this] def makeDevelopersXml(users: Map[String, String]) =
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
}
