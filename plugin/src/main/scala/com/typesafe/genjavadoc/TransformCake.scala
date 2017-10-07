package com.typesafe.genjavadoc

import scala.tools.nsc.Global

trait TransformCake extends JavaSig with Output with Comments with BasicTransform with AST {

  val global: Global
  import global._

  def superTransformUnit(unit: CompilationUnit): Unit

  def superTransform(tree: Tree): Tree

  def javaKeywords: Set[String]

  def filteredStrings: Set[String]
}
