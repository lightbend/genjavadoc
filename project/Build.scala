 /**
  *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
  */

package genjavadoc

import sbt._
import sbt.Keys._

object B extends Build {
  
  override lazy val settings = super.settings ++ Seq(
    organization := "com.typesafe.genjavadoc",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.0"
  )

  lazy val top = Project(
    id = "top",
    base = file("."),
    aggregate = Seq(plugin, tests, javaOut),
    settings = Project.defaultSettings ++ Seq(
      publishArtifact := false
    )
  )

  lazy val plugin = Project(
    id = "genjavadoc-plugin",
    base = file("plugin"),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _),
      crossVersion := CrossVersion.full,
      exportJars := true
    )
  )

  lazy val tests = Project(
    id = "tests",
    base = file("tests"),
    settings = Project.defaultSettings ++ Seq(
      publishArtifact := false,
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      ),
      browse := false,
      scalacOptions in Compile <<= (packageBin in plugin in Compile, scalacOptions in Compile, clean, browse) map (
        (pack, opt, clean, b) => 
          opt ++ 
          Seq("-Xplugin:" + pack.getAbsolutePath, "-P:genjavadoc:out=tests/target/java", "-P:genjavadoc:suppressSynthetic=false") ++ 
          (if (b) Seq("-Ybrowse:uncurry") else Nil)
      )
    )
  )

  lazy val javaOut = Project(
    id = "javaOut",
    base = file("javaOut"),
    settings = Project.defaultSettings ++ Seq(
      publishArtifact := false,
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test"
      ),
      unmanagedSources in Compile <<= (baseDirectory in tests, compile in tests in Test) map ((b, c) => (b / "target/java/akka" ** "*.java").get)
    )
  )

  lazy val browse = SettingKey[Boolean]("browse", "run with -Ybrowse:uncurry")

}
