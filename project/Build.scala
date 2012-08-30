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
    scalaVersion := "2.10.0-M7"
  )

  lazy val top = Project(
    id = "top",
    base = file("."),
    aggregate = Seq(plugin, tests)
  )

  lazy val plugin = Project(
    id = "plugin",
    base = file("plugin"),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _),
      exportJars := true
    )
  )

  lazy val tests = Project(
    id = "tests",
    base = file("tests"),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.scalatest" % "scalatest" % "1.9-2.10.0-M7-B1" % "test" cross CrossVersion.full
      ),
      browse := false,
      scalacOptions in Compile <<= (packageBin in plugin in Compile, scalacOptions in Compile, clean, browse) map (
        (pack, opt, clean, b) => opt ++ Seq("-Xplugin:" + pack.getAbsolutePath) ++ (if (b) Seq("-Ybrowse:uncurry") else Nil)
      )
    )
  )
  
  lazy val browse = SettingKey[Boolean]("browse", "run with -Ybrowse:uncurry")

}
