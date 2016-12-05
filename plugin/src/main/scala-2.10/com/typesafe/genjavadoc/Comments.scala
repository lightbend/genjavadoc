package com.typesafe.genjavadoc

import scala.tools.nsc.ast.parser.SyntaxAnalyzer

trait Comments extends BaseComments { this: TransformCake =>
  object parser extends {
    val global: Comments.this.global.type = Comments.this.global
    val runsAfter = List[String]()
    val runsRightAfter = None
  } with SyntaxAnalyzer

  override def parseComments(): Unit =
    new parser.UnitParser(unit) {
      override def newScanner = new parser.UnitScanner(unit) {
        // This is for 2.10
        override def foundComment(text: String, start: Int, end: Int) {
          val pos = global.rangePos(source, start, start, end)
          comments += pos -> Comment(pos, text)
        }
      }
    }.parse()
}
