package scala.javadoc

import scala.reflect.internal.Flags

trait AST { this: TransformCake ⇒

  import global._

  trait Templ {
    def name: String
    def sig: String
  }

  case class ClassInfo(
    name: String,
    pattern: String ⇒ String,
    module: Boolean,
    comment: Seq[String],
    pckg: String,
    filepattern: String ⇒ String,
    members: Vector[Templ],
    interface: Boolean,
    var firstConstructor: Boolean) extends Templ {

    def sig = pattern(name)
    def file = filepattern(name)

    def addMember(t: Templ) = copy(members = members :+ t)
    def constructor: Boolean = {
      val ret = firstConstructor
      firstConstructor = false
      ret
    }
  }
  object ClassInfo {
    def apply(c: ImplDef, comment: Seq[String]): ClassInfo = {
      c match {
        case ClassDef(mods, name, tparams, impl) ⇒
          val acc = access(mods)
          val fl = flags(mods)
          val kind = if (mods.isInterface || mods.isTrait) "interface" else "class"
          val name = c.name.toString
          val parent = {
            val p = impl.parents.head
            if (p.isEmpty || p.symbol == definitions.ObjectClass) ""
            else {
              s" extends ${js(c.symbol, p.tpe)}"
            }
          }
          val intf = impl.parents.tail map (i ⇒ js(c.symbol, i.tpe)) mkString (", ")
          val interfaces = if (!intf.isEmpty) (if (mods.isInterface) " extends " else " implements ") + intf else ""
          val sig = (n: String) ⇒ s"$acc $fl $kind $n$parent$interfaces"
          val file = (n: String) ⇒ s"${c.symbol.enclosingPackage.fullName('/')}/$n.java"
          val pckg = c.symbol.enclosingPackage.fullName
          ClassInfo(name, sig, mods.hasModuleFlag, comment, pckg, file, Vector.empty, kind == "interface", true)
      }
    }
  }

  case class MethodInfo(pattern: String ⇒ String, ret: String, name: String, comment: Seq[String]) extends Templ {
    def sig = pattern(s"$ret $name")
  }
  object MethodInfo {
    def apply(d: DefDef, dummyImpl: Boolean, comment: Seq[String]): MethodInfo = {
      val acc = access(d.mods) + methodFlags(d.mods)
      val (ret, name) =
        if (d.name == nme.CONSTRUCTOR) {
          ("", d.symbol.enclClass.name.toString)
        } else (js(d.symbol, d.tpt.tpe), d.name.toString)
      val tp = d.symbol.owner.thisType.memberInfo(d.symbol) match {
        case p @ PolyType(params, _) ⇒ js(d.symbol, p)
        case _                       ⇒ ""
      }
      val args = d.vparamss.head map (p ⇒ s"${js(d.symbol, p.tpt.tpe)} ${p.name}") mkString ("(", ", ", ")")
      val impl = if (d.mods.isDeferred || !dummyImpl) ";" else "{ throw new RuntimeException(); }"
      val pattern = (n: String) ⇒ s"$acc $tp $n $args $impl"
      MethodInfo(pattern, ret, name, comment)
    }
  }

  def access(m: Modifiers): String = {
    if (m.isPublic) "public"
    else if (m.isProtected) "protected"
    else if (m.isPrivate) "private"
    else "private" // this is the case for “private[xy]”
  }

  def flags(m: Modifiers): String = {
    var f: List[String] = Nil
    if (m.isFinal) f ::= "final"
    if (m.hasAbstractFlag && !m.isInterface) f ::= "abstract"
    f mkString " "
  }
  
  def methodFlags(m: Modifiers): String = {
    var f: List[String] = Nil
    if (m.isFinal) f ::= "final"
    if (m.isDeferred) f ::= "abstract"
    (if (f.nonEmpty) " " else "") + f.mkString(" ")
  }

}