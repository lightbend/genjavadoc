package com.typesafe.genjavadoc

import scala.reflect.internal.util.RangePosition
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
        // This is for 2.11
        private var docBuffer: StringBuilder = null // buffer for comments (non-null while scanning)
        private var inDocComment = false // if buffer contains double-star doc comment

        override protected def putCommentChar() {
          if (inDocComment)
            docBuffer append ch

          nextChar()
        }
        override def skipDocComment(): Unit = {
          inDocComment = true
          docBuffer = new StringBuilder("/**")
          super.skipDocComment()
        }
        override def skipBlockComment(): Unit = {
          inDocComment = false
          docBuffer = new StringBuilder("/*")
          super.skipBlockComment()
        }
        override def skipComment(): Boolean = {
          // emit a block comment; if it's double-star, make Doc at this pos
          def foundStarComment(start: Int, end: Int) = try {
            val str = docBuffer.toString
            val pos = new RangePosition(unit.source, start, start, end)
            comments += pos.point -> Comment(pos, str)
            true
          } finally {
            docBuffer = null
            inDocComment = false
          }
          super.skipComment() && ((docBuffer eq null) || foundStarComment(offset, charOffset - 2))
        }
      }
    }.parse()
}
