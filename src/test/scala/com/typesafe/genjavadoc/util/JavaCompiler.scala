package com.typesafe.genjavadoc
package util

import javax.tools.StandardLocation
import javax.tools.ToolProvider
import scala.collection.JavaConverters._

/** An instance of the Java compiler. */
class JavaCompiler {

  private val compiler = ToolProvider.getSystemJavaCompiler()

  val target = IO.tempDir("java-classes")

  private val fileManager = compiler.getStandardFileManager(null, null, null)
  fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Seq(target).toIterable.asJava)

  private var _hasErrors = false
  def hasErrors = _hasErrors

  def compile(fileNames: Seq[String]): Unit = {
    val units = fileManager.getJavaFileObjects(fileNames: _*)
    val success = compiler.getTask(null, fileManager, null, null, null, units).call()

    if (!success) _hasErrors = true
  }

}
