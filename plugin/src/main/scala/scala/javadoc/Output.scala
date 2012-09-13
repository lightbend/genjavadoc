package scala.javadoc

import java.io.PrintStream
import java.io.File

trait Output { this: TransformCake =>
  
  def outputBase: File

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
    val f = new File(outputBase, name)
    println(f)
    f.getParentFile.mkdirs
    val w = new PrintStream(f)
    new Out {
      def println(s: String) { w.println(s) }
    }
  }

  def flatten(c: Seq[ClassInfo]): Seq[ClassInfo] = {
    val (obj: Seq[ClassInfo], cls: Seq[ClassInfo]) = c partition (_.module)
    cls
  }

}