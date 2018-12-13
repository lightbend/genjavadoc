package com.typesafe.genjavadoc
package util

import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File

import org.junit.Test
import org.junit.Assert._

/** Utility trait for testing compiler behaviour. */
trait CompilerSpec {

  /** Sources to compile. */
  def sources: Seq[String]

  /** Root directory that contains expected Java output. */
  def expectedPath: String

  /** Extra plugin arguments. */
  def extraSettings: Seq[String] = Seq.empty

  @Test def compileSourcesAndGenerateExpectedOutput(): Unit = {
    val doc = IO.tempDir("java")
    val docPath = doc.getAbsolutePath
    val defaultSettings = Seq(s"out=$docPath", "suppressSynthetic=false")
    val scalac = new GenJavadocCompiler((defaultSettings ++ extraSettings).map{ kv =>
      s"genjavadoc:$kv"
    })

    scalac.compile(sources)
    assertFalse("Scala compiler reported errors", scalac.reporter.hasErrors)

    lines(run(".", "diff", "-wurN",
      "-I", "^ *//", // comment lines
      "-I", "^ *private  java\\.lang\\.Object readResolve", // since Scala 2.12.0-M3, these methods are emitted in a later compiler phase
      expectedPath, docPath)) foreach println
  }

  private def run(dir: String, cmd: String*): Process = {
    new ProcessBuilder(cmd: _*).directory(new File(dir)).redirectErrorStream(true).start()
  }

  private def lines(proc: Process) = {
    val b = new BufferedReader(new InputStreamReader(proc.getInputStream()))
    Iterator.continually(b.readLine).takeWhile(
      _ != null || { proc.waitFor(); assert(proc.exitValue == 0); false }
    )
  }


}
object CompilerSpec {
  def traverseDirectory(dir: File): Seq[File] = {
    dir.listFiles.flatMap { file: File =>
      if (file.isFile) Seq(file)
      else if (file.isDirectory) traverseDirectory(file)
      else Seq.empty
    }
  }
}