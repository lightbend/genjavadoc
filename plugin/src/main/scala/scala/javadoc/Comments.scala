package scala.javadoc

import scala.collection.immutable.TreeMap
import scala.tools.nsc.ast.parser.SyntaxAnalyzer

trait Comments { this: TransformCake ⇒
  import global._

  def unit: CompilationUnit

  object parser extends {
    val global: Comments.this.global.type = Comments.this.global
    val runsAfter = List[String]()
    val runsRightAfter = None
  } with SyntaxAnalyzer

  case class Comment(pos: Position, text: Seq[String])
  object Comment {
    def apply(pos: Position, text: String) = {
      val lines = text.replaceAll("\n[ \t]*", "\n ").split("\n")
        .map(_.replace("{{{", "<pre><code>").replace("}}}", "</code></pre>"))
      new Comment(pos, lines)
    }
  }
  var pos: Position = rangePos(unit.source, 0, 0, 0)

  implicit val positionOrdering: Ordering[Position] = new Ordering[Position] {
    def compare(a: Position, b: Position) =
      if (a.endOrPoint < b.startOrPoint) -1
      else if (a.startOrPoint > b.endOrPoint) 1
      else 0
  }
  var comments = TreeMap[Position, Comment]()

  new parser.UnitParser(unit) {
    override def newScanner = new parser.UnitScanner(unit) {
      override def foundComment(text: String, start: Int, end: Int) {
        val pos = global.rangePos(source, start, start, end)
        comments += pos -> Comment(pos, text)
      }
    }
  }.parse()

  val positions = comments.keySet

  def between(p1: Position, p2: Position) = unit.source.content.slice(p1.startOrPoint, p2.startOrPoint).filterNot(_ == '\n').mkString

  object ScalaDoc extends (Comment ⇒ Boolean) {
    def apply(c: Comment): Boolean = c.text.head.startsWith("/**")
  }

}