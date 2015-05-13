package com.typesafe.genjavadoc

import scala.collection.immutable.TreeMap
import scala.tools.nsc.ast.parser.SyntaxAnalyzer
import scala.annotation.tailrec

trait BaseComments { this: TransformCake ⇒
  import global._

  def unit: CompilationUnit

  object parser extends {
    val global: BaseComments.this.global.type = BaseComments.this.global
    val runsAfter = List[String]()
    val runsRightAfter = None
  } with SyntaxAnalyzer

  private val replacements = Seq(
    "{{{" -> "<pre><code>",
    "}}}" -> "</code></pre>",
    "“" -> "&ldquo;",
    "”" -> "&rdquo;",
    "‘" -> "&lsquo;",
    "’" -> "&rsquo;",
    "&" -> "&amp;",
    "<p/>" -> "<p></p>",
    "[[" -> "{@link ",
    "]]" -> "}")
  private val EmptyLine = """(?:/\*\*(?:.*\*/)?|\s+(?:\*/|\*?))\s*""".r
  private val See = """(.*@see )\[\[([^]]+)]]\s*""".r
  private val HttpLink = "^https?://.*".r

  case class Comment(pos: Position, text: Seq[String])
  object Comment {
    def apply(pos: Position, text: String) = {
      val ll = text.replaceAll("\n[ \t]*", "\n ").split("\n")
        .map {
          case See(prefix, link) ⇒
            if (HttpLink.findFirstIn(link).isDefined)
              s"""$prefix<a href="$link"/>"""
            else
              s"$prefix$link"
          case x ⇒ x
        }
        .map(line ⇒ (line /: replacements) { case (l, (from, to)) ⇒ l.replace(from, to) })
      val (_, _, _, l2) = ((false, false, true, List.empty[String]) /: ll) {
        // insert <p> line upon transition to empty, collapse contiguous empty lines
        case ((pre, code, empty, lines), line @ EmptyLine()) ⇒
          val nl =
            if (pre || line.contains("/**") || line.contains("*/")) line :: lines
            else if (!pre && !empty) " * <p>" :: (lines.head + (if (code) "</code>" else "")) :: lines.tail
            else lines
          (pre, false, true, nl)
        case ((pre, code, empty, lines), line) ⇒
          val (nc, nl) = if (pre) (code, line) else codeLine(code, line)
          val np = if (line contains "<pre>") true else if (line contains "</pre>") false else pre
          val nl2 = if (pre && np) preLine(nl) else nl
          (np, nc, false, nl2 :: lines)
      }
      new Comment(pos, l2.reverse map htmlEntity)
    }
    private def preLine(line: String): String =
      line.replace("@", "&#64;").replace("<", "&lt;").replace(">", "&gt;")
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
      str flatMap (ch ⇒ if (ch > 127) f"&#x${ch}%04x;" else "" + ch)
    }
  }

  implicit val positionOrdering: Ordering[Position] = new Ordering[Position] {
    def compare(a: Position, b: Position) =
      if (a.endOrPoint < b.startOrPoint) -1
      else if (a.startOrPoint > b.endOrPoint) 1
      else 0
  }
  var comments = TreeMap[Position, Comment]()

  // This is overriden in the Scala Version Specific Comments.scala
  protected def parseComments(): Unit

  parseComments()

  val positions = comments.keySet

  def between(p1: Position, p2: Position) = unit.source.content.slice(p1.startOrPoint, p2.startOrPoint).filterNot(_ == '\n').mkString

  object ScalaDoc extends (Comment ⇒ Boolean) {
    def apply(c: Comment): Boolean = c.text.head.startsWith("/**")
  }

}
