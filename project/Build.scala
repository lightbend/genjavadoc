/**
 *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package genjavadoc

import sbt._
import sbt.Keys._

object B extends Build {

  override lazy val settings = super.settings ++ Seq(
    organization := "com.typesafe.genjavadoc",
    version := "0.6-SNAPSHOT",
    scalaVersion := "2.11.0-M6")

  lazy val top = Project(
    id = "top",
    base = file("."),
    aggregate = Seq(plugin, tests, javaOut),
    settings = defaults ++ Seq(
      publishArtifact := false))

  lazy val plugin = Project(
    id = "genjavadoc-plugin",
    base = file("plugin"),
    settings = defaults ++ Seq(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _),
      crossVersion := CrossVersion.full,
      exportJars := true))

  lazy val tests = Project(
    id = "tests",
    base = file("tests"),
    settings = defaults ++ Seq(
      publishArtifact := false,
      libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.11.0-M5" % "2.0.M7" % "test"),
      browse := false,
      scalacOptions in Compile <<= (packageBin in plugin in Compile, scalacOptions in Compile, clean, browse) map (
        (pack, opt, clean, b) ⇒
          opt ++
            Seq("-Xplugin:" + pack.getAbsolutePath, "-P:genjavadoc:out=tests/target/java", "-P:genjavadoc:suppressSynthetic=false") ++
            (if (b) Seq("-Ybrowse:uncurry") else Nil))))

  lazy val javaOut = Project(
    id = "javaOut",
    base = file("javaOut"),
    settings = defaults ++ Seq(
      publishArtifact := false,
      libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest_2.11.0-M5" % "2.0.M7" % "test"),
      unmanagedSources in Compile <<= (baseDirectory in tests, compile in tests in Test) map ((b, c) ⇒ (b / "target/java/akka" ** "*.java").get)))

  lazy val defaults = Project.defaultSettings ++ Seq(
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
}
