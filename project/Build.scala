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
      val scala210and211Versions = (2 to 6).map(i => s"2.10.$i") ++ (0 to 8).map(i => s"2.11.$i")
      ifJavaVersion(_ < 8) {
        scala210and211Versions
      } {
        scala210and211Versions ++ (1 to 5).map(i => s"2.12.0-M$i")
      }
    },
    scalaTestVersion := {
      scalaVersion.value match {
        case "2.12.0-M1" => "2.2.5-M1"
        case "2.12.0-M2" => "2.2.5-M2"
        case "2.12.0-M3" => "2.2.5-M3"
        case "2.12.0-M4" => "2.2.6"
        case "2.12.0-M5" => "3.0.0-RC4"
        case _ => "2.1.3"
      }
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
      unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / sourceDirName(scalaVersion.value),
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

  def sourceDirName(version: String): String = {
    val parts = version.split("\\.").toList
    // this is here to make it easy to compensate for changes in minor versions
    parts match {
      case "2" :: "10" :: _ => "scala-2.10"
      case "2" :: "11" :: _ => "scala-2.11"
      case "2" :: "12" :: _ => "scala-2.12"
      case _ => "unknow-scala-version"
    }
  }

  def ifJavaVersion[T](predicate: Int => Boolean)(yes: => T)(no: => T): T = {
    System.getProperty("java.version").split("\\.").toList match {
      case "1" :: v :: _ if predicate(v.toInt)  => yes
      case _ => no
    }
  }
}
