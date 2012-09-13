package scala.javadoc

import scala.tools.nsc.Global
import scala.tools.nsc.transform.Transform

trait TransformCake extends JavaSig with Output with Comments with BasicTransform with AST {
  
  val global: Global
  import global._
  
  def superTransformUnit(unit: CompilationUnit): Unit
  def superTransform(tree: Tree): Tree

}