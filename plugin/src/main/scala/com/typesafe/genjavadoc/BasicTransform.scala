package com.typesafe.genjavadoc

import scala.reflect.internal.Flags
import java.util.regex.Pattern

trait BasicTransform { this: TransformCake ⇒
  import global._

  private def skippedName(name: String): Boolean = {
    val startsWithNumber = "^\\d".r
    (this.filteredStrings.exists(s => name.contains(s))
      || this.javaKeywords.contains(name)
      || startsWithNumber.findFirstIn(name).isDefined
      || name.equals("$init$"))
  }

  def suppressSynthetic: Boolean

  def newTransformUnit(unit: CompilationUnit): Unit = {
    superTransformUnit(unit)
    for (c ← flatten(classes)) {
      val out = file(c.file)
      try {
        if (c.pckg != "<empty>") out.println(s"package ${c.pckg};")
        write(out, c)
      } finally {
        out.close()
      }
    }
  }

  private var visited: List[Tree] = Nil
  private var keep = true
  private def noKeep(code: ⇒ Tree): Tree = {
    val old = keep
    keep = false
    try code finally keep = old
  }

  private var pos: Position = rangePos(unit.source, 0, 0, 0)

  private var templateMaxPos: Position = pos
  private var prevTemplateMaxPos: Position = pos

  import positionOrdering._
  private def advancePos(p: Position) =
    if (p.isDefined && p > templateMaxPos) templateMaxPos = p

  def newTransform(tree: Tree): Tree = {
    def commentText(tp: Position, endPos: Option[Position]) = {
      val ret = if (tp.isDefined) {
        val old = pos
        pos = max(tp, prevTemplateMaxPos)
        if (old.precedes(pos)) {
          (positions.from(old) intersect positions.to(pos)).toSeq map comments filter ScalaDoc lastOption match {
            case Some(c) ⇒ c.text // :+ s"// found in '${between(old, pos)}'"
            case None ⇒
              // s"// empty '${between(old, pos)}' (${pos.lineContent}:${pos.column})" ::
              Nil
          }
        } else Seq("// not preceding") ++ visited.reverse.map(t ⇒ "// " + global.showRaw(t))
      } else Seq("// no position")
      advancePos(tp)
      endPos foreach { p =>
        advancePos(p)
        pos = max(p, prevTemplateMaxPos)
      }
      visited = Nil
      ret
    }

    def track(t: Tree) = {
      if (!keep && tree.pos.isDefined) {
        visited ::= tree
        pos = tree.pos
      }
      tree
    }

    def endPos(t: Tree) = {
      val traverser = new CollectTreeTraverser({
        case t if t.pos.isDefined ⇒ t.pos
      })
      traverser.traverse(t)
      if (traverser.results.isEmpty) None else Some(traverser.results.max)
    }

    tree match {
      case c: ClassDef if keep ⇒
        withClass(c, commentText(c.pos, None)) {
          superTransform(tree)
        }
      case d: DefDef if keep ⇒
        val (lookat, end) =
          if (d.name == nme.CONSTRUCTOR) {
            if (clazz.get.constructor) (d.symbol.enclClass.pos, None)
            else (d.pos, endPos(d.rhs))
          } else (d.pos, endPos(d.rhs))
        // must be called for keeping the “current” position right
        val text = commentText(lookat, end)
        val name = d.name.toString
        if (!skippedName(name)) {
          if (d.mods.hasFlag(Flags.VARARGS)) addVarargsMethod(d, text)
          else if (!(suppressSynthetic && (d.mods.isSynthetic || d.name == nme.MIXIN_CONSTRUCTOR || name.contains('$'))))
            addMethod(d, text)
        }
        tree
      case _: ValDef     ⇒ { track(tree) }
      case _: PackageDef ⇒ { track(tree); superTransform(tree) }
      case _: Template   ⇒ { track(tree); superTransform(tree) }
      case _: TypeTree   ⇒ { track(tree) }
      case _             ⇒ { track(tree); noKeep(superTransform(tree)) }
    }
  }

  // list of top-level classes in this unit
  private var classes = Vector.empty[ClassInfo]

  // the current class, any level
  private var clazz: Option[ClassInfo] = None

  private def withClass(c: ImplDef, comment: Seq[String])(block: ⇒ Tree): Tree = {
    val old = clazz
    clazz = Some(ClassInfo(c, comment, old.isEmpty))
    val ret = block
    clazz =
      old match {
        case None ⇒
          classes :+= clazz.get
          None
        case Some(oc) ⇒
          Some(oc.addMember(clazz.get))
      }
    pos = templateMaxPos
    prevTemplateMaxPos = templateMaxPos
    ret
  }

  private def addMethod(d: DefDef, comment: Seq[String]) {
    clazz = clazz map (c ⇒ c.addMember(MethodInfo(d, c.interface, comment, hasVararg = false, deprecation = deprecationInfo(d))))
  }

  private def addVarargsMethod(d: DefDef, comment: Seq[String]) {
    clazz = clazz map (c ⇒ c.addMember(MethodInfo(d, c.interface, comment, hasVararg = true, deprecation = deprecationInfo(d))))
  }

  private def deprecationInfo(d: DefDef): Option[DeprecationInfo] =
    if (d.symbol.isDeprecated) {
      val deprec = d.symbol.annotations.find(_.toString contains "deprecated(").get
      Some(DeprecationInfo(deprec.stringArg(0).getOrElse(""), deprec.stringArg(1).getOrElse("")))
    } else None

}
