package com.typesafe.genjavadoc

import org.scalatest.Matchers
import org.scalatest.WordSpec

import util._

object BasicSpec {
  def sources: Seq[String] = Seq(
    "src/test/resources/input/basic/test.scala",
    "src/test/resources/input/basic/root.scala",
    "src/test/resources/input/basic/akka/Main.scala"
  )
}

/** Test basic behaviour of genjavadoc with standard settings */
class BasicSpec extends WordSpec with Matchers with CompilerSpec {

  override def sources = BasicSpec.sources
  override def expectedPath: String = "src/test/resources/expected_output/basic"

}
