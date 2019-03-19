package com.typesafe.genjavadoc

import java.io.File

import com.typesafe.genjavadoc.util.CompilerSpec

class RangePosSpec extends CompilerSpec {
  /** Sources to compile. */
  override def sources: Seq[String] = Seq(
    new File("src/test/resources/input/rangepos/akka/cluster/ClusterRouterGroupSettings.scala").getAbsolutePath
  )

  override def rangepos = true

  /** Root directory that contains expected Java output. */
  override def expectedPath: String =
    new File("src/test/resources/expected_output/rangepos").getAbsolutePath
}
