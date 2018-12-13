package com.typesafe.genjavadoc

import java.io.File

import util._

import scala.sys.process._

object BasicSpec {
  def sources: Seq[String] = CompilerSpec.traverseDirectory(new File("src/test/resources/input/basic")).map(_.getAbsolutePath)
}

/** Test basic behaviour of genjavadoc with standard settings */
class BasicSpec extends CompilerSpec {

  override def sources = BasicSpec.sources
  override def expectedPath: String = {
    val scalaVersion = scala.util.Properties.versionNumberString.split("-").head
    val patchFile = s"src/test/resources/patches/$scalaVersion.patch"
    if (new java.io.File(patchFile).exists) { // we have a patch to apply to expected output for this scala version
      "rm -rf target/expected_output".! // cleanup from previous runs
      "cp -r src/test/resources/expected_output target/".! // copy expected output to a place which is going to be patched
      s"patch -p0 -i $patchFile".! // path expected output
      "target/expected_output/basic"
    } else {
      "src/test/resources/expected_output/basic"
    }
  }

}
