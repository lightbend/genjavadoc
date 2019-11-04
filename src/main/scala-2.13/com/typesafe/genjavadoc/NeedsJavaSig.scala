package com.typesafe.genjavadoc

trait NeedsJavaSig { this: TransformCake =>

  import global._
  import definitions._

  def needsJavaSig(tp: Type): Boolean =
    !settings.Ynogenericsig.value && NeedsSigCollector.collect(tp)

  private object NeedsSigCollector extends TypeCollector(false) {
    private def rebindInnerClass(pre: Type, cls: Symbol): Type =
      if (cls.owner.isClass) cls.owner.tpe else pre // why not cls.isNestedClass?
    // the rest is mostly copy and paste from NeedsSigCollector in nsc/transform/Erasure.scala
    @annotation.tailrec
    private[this] def untilApply(ts: List[Type]): Unit =
      if (! ts.isEmpty && ! result) { apply(ts.head) ; untilApply(ts.tail) }
    override def apply(tp: Type): Unit = {
      if (!result) {
        tp match {
          case st: SubType =>
            apply(st.supertype)
          case TypeRef(pre, sym, args) =>
            if (sym == ArrayClass) untilApply(args)
            else if (sym.isTypeParameterOrSkolem || sym.isExistentiallyBound || !args.isEmpty) result = true
            else if (sym.isClass) apply(rebindInnerClass(pre, sym)) // #2585
            else if (!sym.owner.isPackageClass) apply(pre)
          case PolyType(_, _) | ExistentialType(_, _) =>
            result = true
          case RefinedType(parents, _) =>
            untilApply(parents)
          case ClassInfoType(parents, _, _) =>
            untilApply(parents)
          case at: AnnotatedType =>
            apply(at.underlying)
          case _ =>
            tp.foldOver(this)
        }
      }
    }
  }

}

