package com.typesafe.genjavadoc
import scala.reflect.internal.util.Position
import scala.tools.nsc.doc.ScaladocSyntaxAnalyzer

trait Comments extends BaseComments { this: TransformCake =>
  object parser extends {
    val runsAfter = List[String]()
    val runsRightAfter = None
  } with ScaladocSyntaxAnalyzer[global.type](global)

  override def parseComments(): Unit =
    new parser.ScaladocUnitParser(unit, Nil) {
      override def newScanner = new parser.ScaladocUnitScanner(unit, Nil) {
        override def registerDocComment(str: String, pos: Position) = {
          super.registerDocComment(str, pos)
          comments += pos.point -> Comment(pos, str)
        }
      }
    }.parse()
}
