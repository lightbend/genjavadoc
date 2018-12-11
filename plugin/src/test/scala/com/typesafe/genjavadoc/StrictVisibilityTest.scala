package com.typesafe.genjavadoc

import java.io.File

import util._

/** Test behaviour of genjavadoc with strict visibility enabled */
class StrictVisibilitySpec extends CompilerSpec {

  override def sources = CompilerSpec.traverseDirectory(new File("src/test/resources/input/strict_visibility")).map(_.getAbsolutePath)
  override def expectedPath: String = "src/test/resources/expected_output/strict_visibility"
  override def extraSettings = Seq("strictVisibility=true")

}
