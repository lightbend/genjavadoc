package com.typesafe.genjavadoc

import java.io.PrintStream
import java.io.File

trait Output { this: TransformCake ⇒

  private val Tparam = "(.*@tparam )(\\S+)( .*)".r

  def outputBase: File

  def write(out: Out, c: ClassInfo) {
    // TODO @param should be transformed to constructor comments
    c.comment foreach {line =>
      val replaced = line.replace("@param", "param: ") match {
          case Tparam(_, param, desc) ⇒
            s" * @param <$param> $desc"
          case x ⇒ x
        }
      out(replaced)
    }
    out(s"${c.sig} {")
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
    out(m.sig)
  }

  trait Out {
    var ind = 0

    def println(s: String): Unit
    def apply(s: String) { println(" " * ind + s) }
    def indent() { ind += 2 }
    def outdent() { ind -= 2 }
    def close(): Unit
  }

  def file(name: String): Out = {
    val f = new File(outputBase, name)
    f.getParentFile.mkdirs
    val w = new PrintStream(f, "UTF-8")
    new Out {
      def println(s: String) { w.println(s) }
      def close() { w.close() }
    }
  }

  /**
   * This method is supposed to do the transformation of `object` into
   *
   *  - a class with $ appended to the name
   *  - including a MODULE$ static field
   *  - and static forwarders in the original class
   *
   * The first two parts are to be applied recursively to nested objects.
   */
  def flatten(c: Vector[ClassInfo], forwarders: Boolean = true, staticScope: Boolean = true): Vector[ClassInfo] = {
    val (obj: Vector[ClassInfo], cls: Vector[ClassInfo]) = c collect PreFilter partition (_.module)
    val classes = cls.map(c ⇒ c.name -> c).toMap
    val objects = obj.map(o ⇒ o.name -> o).toMap
    val pairs = obj.map(o ⇒ Some(o) -> (classes get o.name)) ++
      cls.filterNot(c ⇒ objects contains c.name).map(c ⇒ None -> Some(c))
    pairs flatMap { p ⇒
      p match {
        case (Some(o), Some(c))            ⇒ merge(o, c, forwarders, staticScope)
        case (Some(o), None) if forwarders ⇒ merge(o, fabricateCompanion(o), forwarders, staticScope)
        case (Some(o), None)               ⇒ Vector(mangleModule(o, addMODULE = forwarders, pruneClasses = false))
        case (None, Some(c))               ⇒ Vector(c.copy(members = flatten(c.classMembers) ++ c.methodMembers.sortBy(_.name)))
        case (None, None)                  ⇒ ???
      }
    }
  }

  // goes from object to companion class (not the other way around)
  private def fabricateCompanion(obj: ClassInfo): ClassInfo = {
    val com = (obj.comment /: obj.members)((c, mem) ⇒ mem match {
      case x: MethodInfo if x.name == obj.name ⇒ c ++ x.comment
      case _                                   ⇒ c
    })
    val sig = (n: String, a: String) => obj.pattern(n, a).replaceAll(" extends.*", "").replaceAll(" implements.*", "")
    obj.copy(comment = com, module = false, members = Vector.empty, pattern = sig)
  }

  val PreFilter: PartialFunction[ClassInfo, ClassInfo] = {
    case c if c.name != "package" ⇒
      val nm = c.members filterNot (this.javaKeywords contains _.name)
      if (nm == c.members) c
      else c.copy(members = nm)
  }

  private def mangleModule(obj: ClassInfo, addMODULE: Boolean, pruneClasses: Boolean): ClassInfo = {
    val moduleInstance =
      if (addMODULE || (obj.module && obj.static))
        Some(MethodInfo(x ⇒ x, "public static final", s"${obj.name}$$ MODULE$$ = null;",
          Seq("/**", " * Static reference to the singleton instance of this Scala object.", " */")))
      else None
    val members = (moduleInstance ++: obj.members) filter (!pruneClasses || _.isInstanceOf[MethodInfo])
    val (com: Seq[String], moduleMembers: Vector[Templ]) = ((obj.comment, Vector.empty[Templ]) /: members)((p, mem) ⇒ mem match {
      case x: MethodInfo if x.name == obj.name ⇒ (p._1 ++ x.comment, p._2 :+ x.copy(name = x.name + '$', comment = Seq()))
      case x                                   ⇒ (p._1, p._2 :+ x)
    })
    obj.copy(name = obj.name + '$', comment = com, members = moduleMembers)
  }

  private def merge(obj: ClassInfo, cls: ClassInfo, forwarders: Boolean, staticScope: Boolean): Vector[ClassInfo] = {
    val classes = cls.members collect { case c: ClassInfo ⇒ c }
    val methods = cls.members collect {
      case m: MethodInfo ⇒
        if (m.ret.endsWith("$") && classes.exists(_.name == m.name))
          m.copy(comment = Seq("/**", " * Accessor for nested Scala object", " * @return (undocumented)", " */"))
        else m
    }
    val staticClasses = obj.members collect {
      case c: ClassInfo ⇒
        c.copy(
          access = if (cls.interface && c.access == "private") "" else c.access,
          pattern = if (staticScope) (n, a) ⇒ "static " + c.pattern(n, a) else c.pattern,
          static = staticScope)
    }
    val staticMethods =
      if (!forwarders || cls.interface) Vector.empty
      else {
        val direct = obj.members.collect {
          case m: MethodInfo if !(m.name == obj.name) && !cls.members.exists(_.name == m.name) ⇒
            m.copy(pattern = n ⇒ "static " + m.pattern(n))
        }
        val exclude = (direct.iterator ++ methods.iterator).map(_.name).toSet ++ this.javaKeywords
        direct ++ inheritedMethods(cls.sym, exclude)
      }
    val nestedClasses = flatten(classes, forwarders = false, staticScope = false)
    val nestedStaticClasses = flatten(staticClasses, forwarders = false, staticScope = staticScope)
    if (forwarders) {
      val base = cls.copy(members = nestedClasses ++ nestedStaticClasses ++ staticMethods ++ methods)
      val mod = mangleModule(obj, addMODULE = forwarders, pruneClasses = true)
      Vector(base) ++ Vector(mod)
    } else {
      val base = cls.copy(members = nestedClasses ++ staticMethods ++ methods)
      val mod = mangleModule(obj, addMODULE = forwarders, pruneClasses = true)
      Vector(base) ++ Vector(mod.copy(members = nestedStaticClasses ++ mod.members))
    }
  }

  private def inheritedMethods(sym: global.Symbol, exclude: Set[String]): Seq[MethodInfo] = {
    import global._
    sym.ancestors.reverse
      .filter(s => s.name != tpnme.Object && s.name != tpnme.Any)
      .flatMap(_.info.nonPrivateDecls)
      .collect { case m: MethodSymbol if !m.isConstructor && !exclude.contains(m.name.toString) => m }
      .foldLeft(Vector.empty[Symbol])((s, m) => s.filterNot(m.allOverriddenSymbols.contains) :+ m)
      .map(MethodInfo(_))
  }

}
