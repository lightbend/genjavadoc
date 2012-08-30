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
import scala.reflect.internal.ClassfileConstants
import java.io.File

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
        val f = new File(name)
        f.getParentFile.mkdirs
        val w = new PrintStream(f)
        new Out {
          def println(s: String) { w.println(s) }
        }
      }

      def flatten(c: Seq[ClassTemplate]): Seq[ClassInfo] = {
        val (cls: Seq[ClassInfo], obj: Seq[ModuleInfo]) = c partition (_.isInstanceOf[ClassInfo])
        cls
      }

      var visited: List[Tree] = Nil
      var keep = true
      def noKeep(code: ⇒ Tree): Tree = {
        val old = keep
        keep = false
        try code finally keep = old
      }

      override def transform(tree: Tree): Tree = {
        def commentText(tp: Position) = {
          val ret = if (tp.isDefined) {
            val old = pos
            pos = tp
            if (old.precedes(pos)) {
              (positions.from(old) intersect positions.to(pos)).toSeq map comments filter ScalaDoc lastOption match {
                case Some(c) ⇒ c.text // :+ s"// found in '${between(old, pos)}'"
                case None ⇒
                  // s"// empty '${between(old, pos)}' (${pos.lineContent}:${pos.column})" ::
                  Nil
              }
            } else Seq("// not preceding") ++ visited.reverse.map(t ⇒ "// " + global.showRaw(t))
          } else Seq("// no position")
          visited = Nil
          ret
        }

        tree match {
          case c: ClassDef if keep ⇒
            withClass(c, commentText(c.pos)) {
              //              global.newRawTreePrinter.print(tree)
              //              println()
              super.transform(tree)
            }
          case d: DefDef if keep ⇒
            val lookat =
              if (d.name == nme.CONSTRUCTOR) {
                if (clazz.get.constructor) d.symbol.enclClass.pos
                else d.pos
              } else d.pos
            addMethod(d, commentText(lookat))
            noKeep(super.transform(tree))
          case _: ValDef     ⇒ tree
          case _: PackageDef ⇒ super.transform(tree)
          case _: Template   ⇒ super.transform(tree)
          case _: TypeTree   ⇒ tree
          case _ ⇒
            if (tree.pos.isDefined) {
              visited ::= tree
              pos = tree.pos
            }
            noKeep(super.transform(tree))
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
          c match {
            case ClassDef(mods, name, tparams, impl) ⇒
              val acc = access(mods)
              val name = c.name.toString
              val parent = {
                val p = impl.parents.head
                if (p.isEmpty) ""
                else {
                  s" extends ${js(c.symbol, p.tpe)}"
                }
              }
              val intf = impl.parents.tail map (i ⇒ js(c.symbol, i.tpe)) mkString (", ")
              val interfaces = if (!intf.isEmpty) " implements " + intf else ""
              val sig = s"$acc class $name$parent$interfaces"
              val file = c.symbol.enclosingTopLevelClass.fullName('/') + ".java"
              ClassInfo(sig, comment, file, Vector.empty, true)
            case _: ModuleDef ⇒
              // ModuleInfo(name, comment, file, Vector.empty, true)
              sys.error("dunno")
          }
        }
      }

      case class MethodInfo(sig: String, name: String, comment: Seq[String]) extends Templ
      object MethodInfo {
        def apply(d: DefDef, comment: Seq[String]): MethodInfo = {
          val acc = access(d.mods)
          val name =
            if (d.name == nme.CONSTRUCTOR) {
              d.symbol.enclClass.name.toString
            } else s"${js(d.symbol, d.tpt.tpe)} ${d.name}"
          val tp = d.symbol.owner.thisType.memberInfo(d.symbol) match {
            case p @ PolyType(params, _) ⇒ js(d.symbol, p)
            case _                       ⇒ ""
          }
          val ret = js(d.symbol, d.tpt.tpe)
          val args = d.vparamss.head map (p ⇒ s"${js(d.symbol, p.tpt.tpe)} ${p.name}") mkString ("(", ", ", ")")
          val sig = s"$acc $tp $name $args"
          MethodInfo(sig, d.name.toString, comment)
        }
      }

      def access(m: Modifiers): String = {
        import Flags._
        if (m.isPublic) "public"
        else if (m.isProtected) "protected"
        else if (m.isPrivate) "private"
        else sys.error("unknown visibility: " + m)
      }

      /**
       * The Java signature of type 'info', for symbol sym. The symbol is used to give the right return
       *  type for constructors.
       */
      def js(sym0: Symbol, info: Type): String = {
        val isTraitSignature = sym0.enclClass.isTrait

        def removeThis(in: Type): Type = {
//          println("transforming " + in)
          in match {
            case ThisType(parent) if !parent.isPackage ⇒ removeThis(parent.tpe)
            case SingleType(parent, name)              ⇒ typeRef(removeThis(parent), name, Nil)
            case TypeRef(pre, sym, args)               ⇒ typeRef(removeThis(pre), sym, args)
            case x                                     ⇒ x
          }
        }

        def superSig(parents: List[Type]) = {
          val ps = (
            if (isTraitSignature) {
              // java is unthrilled about seeing interfaces inherit from classes
              val ok = parents filter (p ⇒ p.typeSymbol.isTrait || p.typeSymbol.isInterface)
              // traits should always list Object.
              if (ok.isEmpty || ok.head.typeSymbol != ObjectClass) ObjectClass.tpe :: ok
              else ok
            } else parents)
          (ps map boxedSig).mkString
        }
        def boxedSig(tp: Type) = jsig(tp, primitiveOK = false)
        def boundsSig(bounds: List[Type]) = {
          val (isTrait, isClass) = bounds partition (_.typeSymbol.isTrait)
          val classPart = isClass match {
            case Nil    ⇒ "" // + boxedSig(ObjectClass.tpe)
            case x :: _ ⇒ " extends " + boxedSig(x)
          }
          classPart :: (isTrait map boxedSig) mkString " implements "
        }
        def paramSig(tsym: Symbol) = tsym.name + boundsSig(hiBounds(tsym.info.bounds))
        def polyParamSig(tparams: List[Symbol]) = (
          if (tparams.isEmpty) ""
          else tparams map paramSig mkString ("<", ", ", ">"))

        // Anything which could conceivably be a module (i.e. isn't known to be
        // a type parameter or similar) must go through here or the signature is
        // likely to end up with Foo<T>.Empty where it needs Foo<T>.Empty$.
        def fullNameInSig(sym: Symbol): String = sym.name.toString

        def jsig(tp0: Type, existentiallyBound: List[Symbol] = Nil, toplevel: Boolean = false, primitiveOK: Boolean = true): String = {
          val tp = tp0.dealias
          tp match {
            case st: SubType ⇒
              jsig(st.supertype, existentiallyBound, toplevel, primitiveOK)
            case ExistentialType(tparams, tpe) ⇒
              jsig(tpe, tparams, toplevel, primitiveOK)
            case TypeRef(pre, sym, args) ⇒
              def argSig(tp: Type) =
                if (existentiallyBound contains tp.typeSymbol) {
                  val bounds = tp.typeSymbol.info.bounds
                  if (!(AnyRefClass.tpe <:< bounds.hi)) "? extends " + boxedSig(bounds.hi)
                  else if (!(bounds.lo <:< NullClass.tpe)) "? super " + boxedSig(bounds.lo)
                  else "?"
                } else {
                  boxedSig(tp)
                }

              // If args isEmpty, Array is being used as a type constructor
              if (sym == ArrayClass && args.nonEmpty) {
                if (unboundedGenericArrayLevel(tp) == 1) jsig(ObjectClass.tpe)
                else (args map (jsig(_))).mkString + "[]"
              } else if (isTypeParameterInSig(sym, sym0)) {
                assert(!sym.isAliasType, "Unexpected alias type: " + sym)
                sym.name.toString
              } else if (sym == AnyClass || sym == AnyValClass || sym == SingletonClass)
                jsig(ObjectClass.tpe)
              else if (sym == UnitClass)
                jsig(BoxedUnitClass.tpe)
              else if (sym == NothingClass)
                jsig(RuntimeNothingClass.tpe)
              else if (sym == NullClass)
                jsig(RuntimeNullClass.tpe)
              else if (isPrimitiveValueClass(sym)) {
                if (!primitiveOK) jsig(ObjectClass.tpe)
                else if (sym == UnitClass) jsig(BoxedUnitClass.tpe)
                else toJava(tp)
              } else if (sym.isClass) {
                val preRebound = pre.baseType(sym.owner) // #2585
                dotCleanup((
                  if (needsJavaSig(preRebound)) {
                    val s = jsig(preRebound, existentiallyBound)
                    s + "." + sym.javaSimpleName
                  } else fullNameInSig(sym))
                  + (
                    if (args.isEmpty) "" else
                      "<" + (args map argSig).mkString + ">"))
              } else jsig(erasure.erasure(sym0)(tp), existentiallyBound, toplevel, primitiveOK)
            case PolyType(tparams, restpe) ⇒
              assert(tparams.nonEmpty)
              if (toplevel) polyParamSig(tparams) else ""

            case MethodType(params, restpe) ⇒
              "(" + (params map (_.tpe) map (jsig(_))).mkString + ")" +
                (if (restpe.typeSymbol == UnitClass || sym0.isConstructor) ClassfileConstants.VOID_TAG.toString else jsig(restpe))

            case RefinedType(parent :: _, decls) ⇒
              boxedSig(parent)
            case ClassInfoType(parents, _, _) ⇒
              superSig(parents)
            case AnnotatedType(_, atp, _) ⇒
              jsig(atp, existentiallyBound, toplevel, primitiveOK)
            case BoundedWildcardType(bounds) ⇒
              println("something's wrong: " + sym0 + ":" + sym0.tpe + " has a bounded wildcard type")
              jsig(bounds.hi, existentiallyBound, toplevel, primitiveOK)
            case _ ⇒
              val etp = erasure.erasure(sym0)(tp)
              if (etp eq tp) throw new UnknownSig
              else jsig(etp)
          }
        }
        def toJava(info: Type): String = info.dealias.typeSymbol match {
          case UnitClass    ⇒ "void"
          case BooleanClass ⇒ "boolean"
          case ByteClass    ⇒ "byte"
          case ShortClass   ⇒ "short"
          case CharClass    ⇒ "char"
          case IntClass     ⇒ "int"
          case LongClass    ⇒ "long"
          case FloatClass   ⇒ "float"
          case DoubleClass  ⇒ "double"
          case ArrayClass   ⇒ jsig(info)
          case _            ⇒ info.toString.replace('#', '.')
        }
        val _info = removeThis(info)
        if (needsJavaSig(info)) {
          try jsig(_info, toplevel = true)
          catch { case ex: UnknownSig ⇒ toJava(_info) }
        } else toJava(_info)
      }

      private object NeedsSigCollector extends TypeCollector(false) {
        def traverse(tp: Type) {
          if (!result) {
            tp match {
              case st: SubType ⇒
                traverse(st.supertype)
              case TypeRef(pre, sym, args) ⇒
                if (sym == ArrayClass) args foreach traverse
                else if (sym.isTypeParameterOrSkolem || sym.isExistentiallyBound || !args.isEmpty) result = true
                else if (sym.isClass) traverse(rebindInnerClass(pre, sym)) // #2585
                else if (!sym.owner.isPackageClass) traverse(pre)
              case PolyType(_, _) | ExistentialType(_, _) ⇒
                result = true
              case RefinedType(parents, _) ⇒
                parents foreach traverse
              case ClassInfoType(parents, _, _) ⇒
                parents foreach traverse
              case AnnotatedType(_, atp, _) ⇒
                traverse(atp)
              case _ ⇒
                mapOver(tp)
            }
          }
        }
      }

      private def rebindInnerClass(pre: Type, cls: Symbol): Type = {
        if (cls.owner.isClass) cls.owner.tpe else pre // why not cls.isNestedClass?
      }

      // Ensure every '.' in the generated signature immediately follows
      // a close angle bracket '>'.  Any which do not are replaced with '$'.
      // This arises due to multiply nested classes in the face of the
      // rewriting explained at rebindInnerClass.   This should be done in a
      // more rigorous way up front rather than catching it after the fact,
      // but that will be more involved.
      private def dotCleanup(sig: String): String = {
        var last: Char = '\0'
        sig map {
          case '.' if last != '>' ⇒ last = '.'; '$'
          case ch                 ⇒ last = ch; ch
        }
      }

      private def hiBounds(bounds: TypeBounds): List[Type] = bounds.hi.normalize match {
        case RefinedType(parents, _) ⇒ parents map (_.normalize)
        case tp                      ⇒ tp :: Nil
      }

      import erasure.GenericArray

      protected def unboundedGenericArrayLevel(tp: Type): Int = tp match {
        case GenericArray(level, core) if !(core <:< AnyRefClass.tpe) ⇒ level
        case _ ⇒ 0
      }

      private def isTypeParameterInSig(sym: Symbol, initialSymbol: Symbol) = (
        !sym.isHigherOrderTypeParameter &&
        sym.isTypeParameterOrSkolem && (
          (initialSymbol.enclClassChain.exists(sym isNestedIn _)) ||
          (initialSymbol.isMethod && initialSymbol.typeParams.contains(sym))))

      def needsJavaSig(tp: Type) = !settings.Ynogenericsig.value && NeedsSigCollector.collect(tp)

      class UnknownSig extends Exception

    }
  }
}
