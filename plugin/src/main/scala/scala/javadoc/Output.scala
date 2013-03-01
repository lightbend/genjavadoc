package scala.javadoc

import java.io.PrintStream
import java.io.File

trait Output { this: TransformCake ⇒

  def outputBase: File

  def write(out: Out, c: ClassInfo) {
    c.comment foreach (out(_))
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
    out(m.sig + " { throw new RuntimeException(); }")
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
    val f = new File(outputBase, name)
    println(f)
    f.getParentFile.mkdirs
    val w = new PrintStream(f)
    new Out {
      def println(s: String) { w.println(s) }
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
  def flatten(c: Vector[ClassInfo], forwarders: Boolean = true): Vector[ClassInfo] = {
    val (obj: Vector[ClassInfo], cls: Vector[ClassInfo]) = c partition (_.module)
    val classes = cls.map(c ⇒ c.name -> c).toMap
    val objects = obj.map(o ⇒ o.name -> o).toMap
    val pairs = obj.map(o ⇒ Some(o) -> (classes get o.name)) ++
      cls.filterNot(c ⇒ objects contains c.name).map(c ⇒ None -> Some(c))
    pairs flatMap { p ⇒
      p match {
        case (Some(o), Some(c))            ⇒ merge(o, c, forwarders)
        case (Some(o), None) if forwarders ⇒ merge(o, o.copy(comment = Seq(), module = false, members = Vector.empty), forwarders)
        case (Some(o), None)               ⇒ Vector(o.copy(name = o.name + '$', members = o.members collect addDollar(o)))
        case (None, Some(c))               ⇒ Vector(c)
        case (None, None)                  ⇒ ???
      }
    }
  }

  private def addDollar(obj: ClassInfo): PartialFunction[Templ, Templ] = {
    case x: MethodInfo if x.name == obj.name ⇒ x.copy(name = x.name + '$')
    case x                                   ⇒ x
  }

  private def merge(obj: ClassInfo, cls: ClassInfo, forwarders: Boolean): Vector[ClassInfo] = {
    val classes = cls.members collect { case c: ClassInfo ⇒ c }
    val methods = cls.members collect { case m: MethodInfo ⇒ m }
    val staticClasses = obj.members collect { case c: ClassInfo ⇒ c.copy(pattern = n ⇒ "static " + c.pattern(n)) }
    val staticMethods =
      if (!forwarders) Vector.empty
      else obj.members collect { case m: MethodInfo if !cls.members.exists(_.name == m.name) ⇒ m.copy(pattern = n ⇒ "static " + m.pattern(n)) }
    val allClasses = flatten(classes ++ staticClasses, forwarders = false)
    val base = cls.copy(members = allClasses ++ staticMethods ++ methods)
    val module = obj.copy(name = obj.name + '$', members = obj.members filterNot (_.isInstanceOf[ClassInfo]) collect addDollar(obj))
    Vector(base, module)
  }

}