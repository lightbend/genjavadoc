package com.typesafe.genjavadoc

import scala.reflect.internal.Flags
import scala.annotation.tailrec

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
    static: Boolean,
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
    def apply(c: ImplDef, comment: Seq[String], topLevel: Boolean): ClassInfo = {
      c match {
        case ClassDef(mods, name, tparams, impl) ⇒
          val acc = access(mods, topLevel)
          val fl = flags(mods)
          val kind = if (mods.isInterface || mods.isTrait) "interface" else "class"
          val name = c.name.toString
          val tp = c.symbol.owner.thisType.memberInfo(c.symbol) match {
            case p @ PolyType(params, _) ⇒ js(c.symbol, p)
            case _                       ⇒ ""
          }
          val parent = {
            val p = impl.parents.head
            if (p.isEmpty || p.symbol == definitions.ObjectClass || mods.isTrait) ""
            else {
              s" extends ${js(c.symbol, p.tpe)}"
            }
          }
          val intf = impl.parents.tail map (i ⇒ js(c.symbol, i.tpe)) mkString (", ")
          val interfaces = if (!intf.isEmpty) (if (mods.isInterface || mods.isTrait) " extends " else " implements ") + intf else ""
          val sig = (n: String) ⇒ s"$acc $fl $kind $n$tp$parent$interfaces"
          val file = (n: String) ⇒ s"${c.symbol.enclosingPackage.fullName('/')}/$n.java"
          val pckg = c.symbol.enclosingPackage.fullName
          ClassInfo(name, sig, mods.hasModuleFlag, comment, pckg, file, Vector.empty, kind == "interface", false, true)
      }
    }
  }

  case class MethodInfo(pattern: String ⇒ String, ret: String, name: String, comment: Seq[String]) extends Templ {
    def sig = pattern(s"$ret $name")
  }
  object MethodInfo {
    def apply(d: DefDef, interface: Boolean, comment: Seq[String], hasVararg: Boolean): MethodInfo = {
      val acc = methodAccess(d.mods, interface) + methodFlags(d.mods, interface)
      val (ret, name) =
        if (d.name == nme.CONSTRUCTOR) {
          ("", d.symbol.enclClass.name.toString)
        } else (js(d.symbol, d.tpt.tpe), d.name.toString)
      val tp = d.symbol.owner.thisType.memberInfo(d.symbol) match {
        case p @ PolyType(params, _) ⇒ js(d.symbol, p)
        case _                       ⇒ ""
      }
      @tailrec def rec(l: List[ValDef], acc: Vector[String] = Vector.empty): Seq[String] = l match {
        case x :: Nil if hasVararg ⇒ acc :+ s"${js(d.symbol, x.tpt.tpe, voidOK = false).dropRight(2)}... ${mangleMethodName(x)}"
        case x :: xs               ⇒ rec(xs, acc :+ s"${js(d.symbol, x.tpt.tpe, voidOK = false)} ${mangleMethodName(x)}")
        case Nil                   ⇒ acc
      }
      val args = rec(d.vparamss.head) mkString ("(", ", ", ")")
      val impl = if (d.mods.isDeferred || interface) ";" else "{ throw new RuntimeException(); }"
      val pattern = (n: String) ⇒ s"$acc $tp $n $args $impl"
      MethodInfo(pattern, ret, name, comment)
    }
  }

  def mangleMethodName(p: ValDef): String = {
    if (this.javaKeywords contains p.name.toString) s"${p.name}_" else p.name.toString
  }

  def access(m: Modifiers, topLevel: Boolean): String = {
    if (m.isPublic) "public"
    else if (m.isProtected && !topLevel) "protected"
    else if (m.isPrivate && !topLevel) {
      if (m.isInterface || m.hasStaticFlag) "" else "private"
    } else "public" // this is the case for “private[xy]” and top level classes
  }

  def methodAccess(m: Modifiers, interface: Boolean): String = {
    if (m.isPublic) "public"
    else if (m.isProtected && !interface) "protected"
    else if (m.isPrivate && !interface) "private"
    else "public" // this is the case for “private[xy]” and interfaces
  }

  def flags(m: Modifiers): String = {
    var f: List[String] = Nil
    if (m.isFinal) f ::= "final"
    if (m.hasAbstractFlag && !(m.isInterface || m.isTrait)) f ::= "abstract"
    f mkString " "
  }

  def methodFlags(m: Modifiers, interface: Boolean): String = {
    var f: List[String] = Nil
    if (m.isFinal && !interface) f ::= "final"
    if (m.isDeferred && !interface) f ::= "abstract"
    (if (f.nonEmpty) " " else "") + f.mkString(" ")
  }

}
