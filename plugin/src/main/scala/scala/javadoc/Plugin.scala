package scala.javadoc

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.{ Transform, TypingTransformers }
import nsc.symtab.Flags
import collection.immutable.TreeMap
import java.io.PrintStream
import scala.tools.nsc.ast.parser.SyntaxAnalyzer
import scala.reflect.internal.util.NoPosition

class GenJavaDocPlugin(val global: Global) extends Plugin {
  import global._

  val name = "GenJavaDoc"
  val description = ""
  val components = List[PluginComponent](MyComponent)

  private object MyComponent extends PluginComponent with Transform {

    import global._
    import global.definitions._

    type GT = GenJavaDocPlugin.this.global.type

    override val global: GT = GenJavaDocPlugin.this.global

    override val runsAfter = List("uncurry")
    val phaseName = "GenJavaDoc"

    def newTransformer(unit: CompilationUnit) = new GenJavaDocTransformer(unit)

    object parser extends {
      val global: GT = MyComponent.this.global
      val runsAfter = List[String]()
      val runsRightAfter = None
    } with SyntaxAnalyzer

    class GenJavaDocTransformer(val unit: CompilationUnit) extends Transformer {

      case class Comment(pos: Position, text: Seq[String])
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
            comments += pos -> Comment(pos, cleanup(text))
          }
          private def cleanup(s: String) = s.replaceAll("\n[ \t]*", "\n ").split("\n")
        }
      }.parse()

      val positions = comments.keySet

      override def transformUnit(unit: CompilationUnit): Unit = {
        super.transformUnit(unit)
        for (c ← flatten(classes)) {
          write(file(c.file), c)
        }
      }

      def write(out: Out, c: ClassInfo) {
        c.comment foreach (out(_))
        out(s"${c.sig} { // ${c.file}")
        out.indent()
        for (m ← c.members)
          m match {
            case clazz: ClassInfo   ⇒ write(out, clazz)
            case method: MethodInfo ⇒ write(out, method)
          }
        out.outdent()
        out("}")
      }

      def write(out: Out, m: MethodInfo) {
        m.comment foreach (out(_))
        out(m.sig + " {}")
      }

      trait Out {
        var ind = 0

        def println(s: String): Unit
        def apply(s: String) { println(" " * ind + s) }
        def indent() { ind += 2 }
        def outdent() { ind -= 2 }
      }

      def file(name: String): Out = {
        println("*** " + name)
        new Out {
          def println(s: String) { System.out.println(s) }
        }
      }

      def flatten(c: Seq[ClassTemplate]): Seq[ClassInfo] = {
        val (cls: Seq[ClassInfo], obj: Seq[ModuleInfo]) = c partition (_.isInstanceOf[ClassInfo])
        cls
      }

      var visited: List[Tree] = Nil

      override def transform(tree: Tree): Tree = {
        def commentText(tp: Position) = {
          val ret = if (tp.isDefined) {
            val old = pos
            pos = tp
            if (old.precedes(pos)) {
              (positions.from(old) intersect positions.to(pos)).toSeq map comments filter ScalaDoc lastOption match {
                case Some(c) ⇒ c.text :+ s"// found in '${between(old, pos)}'"
                case None ⇒
                  Seq(s"// empty '${between(old, pos)}' (${pos.lineContent}:${pos.column})")
              }
            } else Seq("// not preceding") ++ visited.reverse.map(t ⇒ "// " + global.showRaw(t))
          } else Seq("// no position")
          visited = Nil
          ret
        }

        tree match {
          case c: ClassDef ⇒
            withClass(c, commentText(c.pos)) {
              global.newRawTreePrinter.print(tree)
              println()
              super.transform(tree)
            }
          case d: DefDef ⇒
            val lookat =
              if (d.name.toString == "<init>") {
                if (clazz.get.constructor) d.symbol.enclClass.pos
                else d.pos
              } else d.pos
            addMethod(d, commentText(lookat))
            tree
          case o: ModuleDef  ⇒ withClass(o, commentText(o.pos))(super.transform(tree))
          case _: ValDef     ⇒ tree
          case _: PackageDef ⇒ super.transform(tree)
          case _: Template   ⇒ super.transform(tree)
          case _: TypeTree   ⇒ tree
          case _ ⇒
            if (tree.pos.isDefined) {
              visited ::= tree
              pos = tree.pos
            }
            tree
        }
      }

      def between(p1: Position, p2: Position) = unit.source.content.slice(p1.startOrPoint, p2.startOrPoint).filterNot(_ == '\n').mkString

      // list of top-level classes in this unit
      var classes = Vector.empty[ClassTemplate]

      // the current class, any level
      var clazz: Option[ClassTemplate] = None

      def withClass(c: ImplDef, comment: Seq[String])(block: ⇒ Tree): Tree = {
        val old = clazz
        clazz = Some(ClassInfo(c, comment))
        val ret = block
        clazz = old match {
          case None     ⇒ classes :+= clazz.get; None
          case Some(oc) ⇒ Some(oc.addMember(clazz.get))
        }
        ret
      }

      def addMethod(d: DefDef, comment: Seq[String]) {
        clazz = clazz map (_.addMember(MethodInfo(d, comment)))
      }

      object ScalaDoc extends (Comment ⇒ Boolean) {
        def apply(c: Comment): Boolean = c.text.head.startsWith("/**")
      }

      trait Templ
      trait ClassTemplate extends Templ {
        def addMember(t: Templ): ClassTemplate
        def members: Seq[Templ]
        def sig: String
        def firstConstructor: Boolean
        def firstConstructor_=(b: Boolean): Unit
        def constructor: Boolean = {
          val ret = firstConstructor
          firstConstructor = false
          ret
        }
      }

      case class ClassInfo(sig: String, comment: Seq[String], file: String, members: Vector[Templ], var firstConstructor: Boolean) extends ClassTemplate {
        def addMember(t: Templ) = copy(members = members :+ t)
      }
      case class ModuleInfo(sig: String, comment: Seq[String], file: String, members: Vector[Templ], var firstConstructor: Boolean) extends ClassTemplate {
        def addMember(t: Templ) = copy(members = members :+ t)
      }
      object ClassInfo {
        def apply(c: ImplDef, comment: Seq[String]): ClassTemplate = {
          val name = c.name.toString
          val file = c.symbol.enclosingTopLevelClass.fullName('/') + ".java"
          c match {
            case _: ClassDef  ⇒ ClassInfo(name, comment, file, Vector.empty, true)
            case _: ModuleDef ⇒ ModuleInfo(name, comment, file, Vector.empty, true)
          }
        }
      }

      case class MethodInfo(sig: String, name: String, comment: Seq[String]) extends Templ
      object MethodInfo {
        def apply(d: DefDef, comment: Seq[String]): MethodInfo = {
          MethodInfo(d.name.toString, d.name.toString, comment)
        }
      }
    }
  }
}
