package com.typesafe.genjavadoc

import scala.annotation.tailrec

trait AST { this: TransformCake ⇒

  import global._

  def strictVisibility: Boolean

  trait Templ {
    def name: String
    def sig: String
    def access: String
  }

  case class ClassInfo(
    sym: Symbol,
    name: String,
    access: String,
    pattern: (String, String) ⇒ String,
    module: Boolean,
    comment: Seq[String],
    pckg: String,
    filepattern: String ⇒ String,
    members: Vector[Templ],
    interface: Boolean,
    static: Boolean,
    var firstConstructor: Boolean) extends Templ {

    def sig = pattern(name, access)
    def file = filepattern(name)

    def addMember(t: Templ) = copy(members = members :+ t)

    def constructor: Boolean = {
      val ret = firstConstructor
      firstConstructor = false
      ret
    }

    def classMembers = members.collect { case classInfo: ClassInfo => classInfo }
    def methodMembers = members.collect { case methodInfo: MethodInfo => methodInfo }

    override def toString =
      s"ClassInfo($name, ${pattern("XXXXX", "AAAAA")}, module=$module, pckg=$pckg, ${filepattern("FFFFFF")}, interface=$interface, static=$static)" +
        comment.mkString("\n  ", "\n  ", "\n  ") + members.mkString("\n  ")
  }
  object ClassInfo {
    def apply(c: ImplDef, comment: Seq[String], topLevel: Boolean): ClassInfo = {
      c match {
        case ClassDef(mods, _, tparams, impl) ⇒
          val name = c.name.toString
          val acc = access(mods, topLevel)
          val fl = flags(mods)
          val kind = if (mods.isInterface || mods.isTrait) "interface" else "class"
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
          val sig = (n: String, a: String) ⇒ s"$a $fl $kind $n$tp$parent$interfaces"
          val packageName = c.symbol.enclosingPackage.fullName('/')
          val file =
            if (packageName == "<empty>") (n: String) => s"$n.java"
            else (n: String) ⇒ s"$packageName/$n.java"
          val pckg = c.symbol.enclosingPackage.fullName
          ClassInfo(c.symbol, name, acc, sig, mods.hasModuleFlag, comment, pckg, file, Vector.empty, kind == "interface", false, true)
      }
    }
  }

  def fabricateParams: Boolean

  case class DeprecationInfo(msg: String, since: String) {
    def maybeDot = if (msg.endsWith(".")) " " else ". "
    def maybeSinceDot = if (since.endsWith(".")) " " else ". "
    def render = s" * @deprecated ${msg}${maybeDot}Since $since${maybeSinceDot}"

    def appendToComment(comment: Seq[String]): Seq[String] = comment match {
      case c if c.lastOption.contains(" */") =>
        c.init ++ List(" *", render) :+ c.last
      case c =>
        c ++ List("/**", render, "*/")
    }
  }

  case class MethodInfo(access: String, pattern: String ⇒ String, ret: String, name: String, comment: Seq[String]) extends Templ {
    def sig = pattern(s"$ret $name")
  }
  object MethodInfo {
    def apply(d: DefDef, interface: Boolean, comment: Seq[String], hasVararg: Boolean, deprecation: Option[DeprecationInfo]): MethodInfo = {
      val acc = methodAccess(d.symbol, interface) + methodFlags(d.mods, interface)
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

      val throwsAnnotations = d.symbol.annotations.collect {
        case ThrownException(exc) => (exc: Any) match {
          case s: Symbol => s.fullName
          case t: Type => t.typeSymbol.fullName // 2.12.0-M4+ has a Type instead of a TypeSymbol in ThrownException
        }
      }
      val throws = if (throwsAnnotations.isEmpty) "" else "throws " + throwsAnnotations.mkString(", ")

      val impl = if (d.mods.isDeferred || interface) ";" else "{ throw new RuntimeException(); }"
      val pattern = (n: String) ⇒ s"$acc $tp $n $args $throws $impl"
      def hasParam(n: String) = comment.find(_.contains(s"@param $n")).isDefined

      val commentWithParams =
        if (fabricateParams && comment.size > 1 && comment.head.startsWith("/**")) {
          val p = d.vparamss.head.map(mangleMethodName).filterNot(hasParam)
          val rev = comment.toList.reverse
          val r = if (ret == "void" || ret == "" || comment.find(_.contains("@return")).isDefined) Nil else " * @return (undocumented)" :: Nil
          rev.tail reverse_::: p.map(n => s" * @param $n (undocumented)") ::: r ::: rev.head :: Nil
        } else comment
      val commentWithParamsAndDeprec =
        if (comment.exists(_ contains "* @deprecated ")) commentWithParams // skip adding deprecated javadoc if already there
        else deprecation match {
          case Some(deprec) => deprec.appendToComment(commentWithParams)
          case _ => commentWithParams
        }
      MethodInfo(acc, pattern, ret, name, commentWithParamsAndDeprec)
    }

    /**
     * This is used only for creating the static forwarders for methods that
     * are inherited by the Scala object in question, hence `interface=false`
     * and the addition of `static`.
     */
    def apply(sym: Symbol): MethodInfo = {
      val varargs = sym match {
        case m: MethodSymbol => m.isVarargsMethod
        case _               => false
      }
      val d = DefDef(sym, EmptyTree)
      val m = MethodInfo(d, false, Nil, varargs, None)
      m.copy(pattern = n ⇒ "static " + m.pattern(n))
    }
  }

  private def mangleMethodName(p: ValDef): String = {
    if (this.javaKeywords contains p.name.toString) s"${p.name}_" else p.name.toString
  }

  private def access(m: Modifiers, topLevel: Boolean): String = {
    if (m.isPublic || m.isTrait) "public"
    else if (m.isProtected && !topLevel) "protected"
    else if (m.isPrivate && !topLevel) {
      if (m.isInterface || m.hasStaticFlag) "" else "private"
    }
    else if (strictVisibility && m.privateWithin != tpnme.EMPTY) ""
    else "public" // this is the case for top level classes
  }

  private def methodAccess(sym: Symbol, interface: Boolean): String = {
    if (sym.isPublic) "public"
    else if (sym.isProtected && !interface) "protected"
    else if (sym.isPrivate && !interface) "private"
    else if (strictVisibility && sym.privateWithin != NoSymbol) ""
    else "public" // this is the case for interfaces
  }

  private def flags(m: Modifiers): String = {
    var f: List[String] = Nil
    if (m.isFinal) f ::= "final"
    if (m.hasAbstractFlag && !(m.isInterface || m.isTrait)) f ::= "abstract"
    f mkString " "
  }

  private def methodFlags(m: Modifiers, interface: Boolean): String = {
    var f: List[String] = Nil
    if (m.isFinal && !interface) f ::= "final"
    if (m.isDeferred && !interface) f ::= "abstract"
    (if (f.nonEmpty) " " else "") + f.mkString(" ")
  }

}
