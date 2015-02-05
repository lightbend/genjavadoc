package com.typesafe.genjavadoc

import org.scalatest.Matchers
import org.scalatest.WordSpec

import util._

/** Test behaviour of genjavadoc with strict visibility enabled */
class StrictVisibilitySpec extends WordSpec with Matchers with CompilerSpec {

  override def sources = Seq("src/test/resources/input/strict_visibility/test.scala")
  override def expectedPath: String = "src/test/resources/expected_output/strict_visibility"
  override def extraSettings = Seq("strictVisibility=true")

}
