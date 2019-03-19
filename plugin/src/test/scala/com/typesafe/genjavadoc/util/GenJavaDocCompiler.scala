package com.typesafe.genjavadoc
package util

import scala.io.Source
import scala.reflect.io.AbstractFile
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.{Global, Settings}

/** An instance of the Scala compiler with the genjavadoc plugin enabled
  * @param pluginOptions additional parameters to pass to the compiler
  */
class GenJavadocCompiler(pluginOptions: Seq[String], rangepos: Boolean) {

  private val settings = new Settings

  settings.Yrangepos.value = rangepos

  val reporter = new ConsoleReporter(settings)
  private val global = new Global(settings, reporter) {
    override protected def loadRoughPluginsList() =
      new GenJavadocPlugin(this) :: super.loadRoughPluginsList()
  }

  val target = IO.tempDir("scala-classes")

  Option(getClass.getResourceAsStream("/embeddedcp")) match {
    case Some(is) =>
      Source.fromInputStream(is).getLines().foreach(settings.classpath.append)
    case None =>
      settings.usejavacp.value = true
  }

  settings.outputDirs.setSingleOutput(AbstractFile.getDirectory(target))
  settings.pluginOptions.value = pluginOptions.toList

  def compile(fileNames: Seq[String]): Unit = {
    val run = new global.Run
    run.compile(fileNames.toList)
  }

}
