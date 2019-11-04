package com.typesafe.genjavadoc

trait NeedsJavaSig { this: TransformCake =>

  import global._
  import definitions._

  def needsJavaSig(tp: Type): Boolean =
    !settings.Ynogenericsig.value && NeedsSigCollector.collect(tp)

  private object NeedsSigCollector extends TypeCollector(false) {
    private def rebindInnerClass(pre: Type, cls: Symbol): Type =
      if (cls.owner.isClass) cls.owner.tpe else pre // why not cls.isNestedClass?
    override def traverse(tp: Type): Unit = {
      if (!result) {
        tp match {
          case st: SubType =>
            traverse(st.supertype)
          case TypeRef(pre, sym, args) =>
            if (sym == ArrayClass) args foreach traverse
            else if (sym.isTypeParameterOrSkolem || sym.isExistentiallyBound || !args.isEmpty) result = true
            else if (sym.isClass) traverse(rebindInnerClass(pre, sym)) // #2585
            else if (!sym.owner.isPackageClass) traverse(pre)
          case PolyType(_, _) | ExistentialType(_, _) =>
            result = true
          case RefinedType(parents, _) =>
            parents foreach traverse
          case ClassInfoType(parents, _, _) =>
            parents foreach traverse
          case at: AnnotatedType =>
            traverse(at.underlying)
          case _ =>
            mapOver(tp)
        }
      }
    }
  }

}

