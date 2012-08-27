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

class GenJavaDocPlugin(val global: Global) extends Plugin {
  import global._

  val name = "GenJavaDoc"
  val description = ""
  val components = List[PluginComponent](MyComponent)
  println("started")

  private object MyComponent extends PluginComponent with Transform {

    import global._
    import global.definitions._
    
    override val global = GenJavaDocPlugin.this.global

    override val runsAfter = List("uncurry")

    val phaseName = "GenJavaDoc"

    def newTransformer(unit: CompilationUnit) = new GenJavaDocTransformer(unit)

    class GenJavaDocTransformer(val unit: CompilationUnit) extends Transformer {

      var pos: Position = rangePos(unit.source, 0, 0, 0)

      implicit val positionOrdering: Ordering[Position] = new Ordering[Position] {
        def compare(a: Position, b: Position) =
          if (a.endOrPoint < b.startOrPoint) -1
          else if (a.startOrPoint > b.endOrPoint) 1
          else 0
      }
      var comments = TreeMap[Position, DocComment]() ++ global.docComments.map { case (k, v) ⇒ (v.pos, v) }
      var positions = comments.keySet

      override def transformUnit(unit: CompilationUnit): Unit = {
        super.transformUnit(unit)
        for (c ← flatten(classes)) {
          write(file(c.file.get), c)
        }
      }

      def write(out: Out, c: ClassInfo) {
        c.comment foreach (out(_))
        out(c.sig + " {")
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

      override def transform(tree: Tree): Tree = {
        val comment =
          if (tree.pos.isDefined) {
            val old = pos
            pos = tree.pos
            if (pos.precedes(tree.pos)) {
              (positions.from(old) intersect positions.to(tree.pos)).toSeq map comments filter ScalaDoc lastOption
            } else None
          } else None
        val commentText = comment map (_.raw)
        tree match {
          case c: ClassDef  ⇒ withClass(c, commentText)(super.transform(tree))
          case d: DefDef    ⇒ addMethod(d, commentText); tree
          case o: ModuleDef ⇒ withClass(o, commentText)(super.transform(tree))
          case _            ⇒ super.transform(tree)
        }
      }

      // list of top-level classes in this unit
      var classes = Vector.empty[ClassTemplate]

      // the current class, any level
      var clazz: Option[ClassTemplate] = None

      def withClass(c: ImplDef, comment: Option[String])(block: ⇒ Tree): Tree = {
        val old = clazz
        clazz = Some(ClassInfo(c, comment))
        val ret = block
        clazz = old match {
          case None     ⇒ classes :+= clazz.get; None
          case Some(oc) ⇒ Some(oc.addMember(clazz.get))
        }
        ret
      }

      def addMethod(d: DefDef, comment: Option[String]) {
        clazz = clazz map (_.addMember(MethodInfo(d, comment)))
      }

      object ScalaDoc extends (global.DocComment ⇒ Boolean) {
        def apply(c: global.DocComment): Boolean = c.raw.startsWith("/**")
      }

      trait Template
      trait ClassTemplate extends Template {
        def addMember(t: Template): ClassTemplate
        def members: Seq[Template]
      }

      case class ClassInfo(sig: String, comment: Option[String], file: Option[String], members: Vector[Template] = Vector.empty) extends ClassTemplate {
        def addMember(t: Template) = copy(members = members :+ t)
      }
      case class ModuleInfo(sig: String, comment: Option[String], file: Option[String], members: Vector[Template] = Vector.empty) extends ClassTemplate {
        def addMember(t: Template) = copy(members = members :+ t)
      }
      object ClassInfo {
        def apply(c: ImplDef, comment: Option[String]): ClassTemplate = {
          val name = c.name.toString
          val file = clazz map (_ ⇒ None) getOrElse Some(c.symbol.fullName('/') + ".java")
          c match {
            case _: ClassDef  ⇒ ClassInfo(name, comment, file)
            case _: ModuleDef ⇒ ModuleInfo(name, comment, file)
          }
        }
      }

      case class MethodInfo(sig: String, name: String, comment: Option[String]) extends Template
      object MethodInfo {
        def apply(d: DefDef, comment: Option[String]): MethodInfo = {
          MethodInfo(d.name.toString, d.name.toString, comment)
        }
      }
    }
  }
}
