package com.typesafe.genjavadoc

import org.scalatest.Matchers
import org.scalatest.WordSpec
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File

import util._

object BasicSpec {
  private val inDir = "src/test/resources/input"
  val scalaSources: Seq[String] = Seq(
    s"$inDir/test.scala",
    s"$inDir/root.scala",
    s"$inDir/akka/Main.scala"
  )
}

/** Test basic behaviour of genjavadoc with standard settings */
class BasicSpec extends WordSpec with Matchers {
  import BasicSpec._

  "GenJavaDoc" must {
    val doc = IO.tempDir("java")
    val docPath = doc.getAbsolutePath
    val scalac = new GenJavaDocCompiler(
      s"genjavadoc:out=$docPath",
      s"genjavadoc:strictVisibility=true",
      "genjavadoc:suppressSynthetic=false"
    )

    "compile Scala sources" in {
      scalac.compile(scalaSources)
      assert(!scalac.reporter.hasErrors, "Scala compiler reported errors.")
    }

    "generate the expected output" in {
      lines(run(".", "diff", "-wurN",
        "-I", "^ *//", // comment lines
        "-I", "^ *private  java\\.lang\\.Object readResolve", // since Scala 2.12.0-M3, these methods are emitted in a later compiler phase
        "src/test/resources/expected_output/akka", s"$docPath/akka")) foreach println
    }

  }

  def run(dir: String, cmd: String*): Process = {
    new ProcessBuilder(cmd: _*).directory(new File(dir)).redirectErrorStream(true).start()
  }

  def lines(proc: Process) = {
    val b = new BufferedReader(new InputStreamReader(proc.getInputStream()))
    Iterator.continually(b.readLine).takeWhile(_ != null || { proc.waitFor(); assert(proc.exitValue == 0); false })
  }

}
