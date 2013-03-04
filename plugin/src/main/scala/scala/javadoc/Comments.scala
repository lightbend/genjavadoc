package scala.javadoc

import scala.collection.immutable.TreeMap
import scala.tools.nsc.ast.parser.SyntaxAnalyzer
import scala.annotation.tailrec

trait Comments { this: TransformCake ⇒
  import global._

  def unit: CompilationUnit

  object parser extends {
    val global: Comments.this.global.type = Comments.this.global
    val runsAfter = List[String]()
    val runsRightAfter = None
  } with SyntaxAnalyzer

  val replacements = Seq(
    "{{{" -> "<pre><code>",
    "}}}" -> "</code></pre>",
    "“" -> "&ldquo;",
    "”" -> "&rdquo;",
    "‘" -> "&lsquo;",
    "’" -> "&rsquo;",
    "[[" -> "{@link ",
    "]]" -> "}")
  val EmptyLine = """(?:/\*\*(?:.*\*/)?|\s+(?:\*/|\*?))\s*""".r

  case class Comment(pos: Position, text: Seq[String])
  object Comment {
    def apply(pos: Position, text: String) = {
      val ll = text.replaceAll("\n[ \t]*", "\n ").split("\n")
        .map(line ⇒ (line /: replacements) { case (l, (from, to)) ⇒ l.replace(from, to) })
      val (_, _, _, l2) = ((false, false, true, List.empty[String]) /: ll) {
        case ((pre, code, empty, lines), line @ EmptyLine()) ⇒
          if (!pre && !empty) (pre, false, true, line :: (lines.head + "</p>") :: lines.tail)
          else (pre, false, true, line :: lines)
        case ((pre, code, empty, lines), line) ⇒
          val (nc, nl) = codeLine(code, line)
          val np = if (line contains "<pre>") true else if (line contains "</pre>") false else pre
          if (!pre && empty) (np, nc, false, nl :: " * <p>" :: lines)
          else (np, nc, false, nl :: lines)
      }
      new Comment(pos, l2.reverse map htmlEntity)
    }
    @tailrec private def codeLine(code: Boolean, line: String): (Boolean, String) = {
      val next = replace(line, "`", if (code) "</code>" else "<code>")
      if (next eq line) (code, line)
      else codeLine(!code, next)
    }
    private def replace(str: String, from: String, to: String): String = {
      str.indexOf(from) match {
        case -1 ⇒ str
        case n  ⇒ str.substring(0, n) + to + str.substring(n + from.length)
      }
    }
    private def htmlEntity(str: String): String = {
      str flatMap (ch => if (ch > 127) f"&#x${ch}%04x;" else "" + ch)
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