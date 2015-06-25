package com.typesafe.genjavadoc

import scala.reflect.internal.ClassfileConstants

trait JavaSig { this: TransformCake ⇒
  import global._
  import definitions._

  def js(sym0: Symbol, info: Type, voidOK: Boolean = true): String = {
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
    def boxedSig(tp: Type) = tp match {
      case PolyType(tparams, restpe) ⇒ jsig(restpe, primitiveOK = false)
      case _                         ⇒ jsig(tp, primitiveOK = false)
    }
    def boundsSig(bounds: List[Type]) = {
      bounds.headOption map (" extends " + boxedSig(_)) getOrElse ""
    }
    def paramSig(tsym: Symbol) = tsym.name + boundsSig(hiBounds(tsym.info.bounds))
    def polyParamSig(tparams: List[Symbol]) = (
      if (tparams.isEmpty) ""
      else tparams map paramSig mkString ("<", ", ", ">"))

    // Anything which could conceivably be a module (i.e. isn't known to be
    // a type parameter or similar) must go through here or the signature is
    // likely to end up with Foo<T>.Empty where it needs Foo<T>.Empty$.
    def fullNameInSig(sym: Symbol): String = sym.fullName + sym.moduleSuffix

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
            val name =
              if (needsJavaSig(preRebound)) {
                val s = jsig(preRebound, existentiallyBound)
                s + "." + sym.javaSimpleName
              } else fullNameInSig(sym)
            val generics =
              if (args.isEmpty) "" else
                "<" + (args map argSig).mkString(", ") + ">"
            name + generics
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
        case at: AnnotatedType ⇒
          jsig(at.underlying, existentiallyBound, toplevel, primitiveOK)
        case BoundedWildcardType(bounds) ⇒
          println("something's wrong: " + sym0 + ":" + sym0.tpe + " has a bounded wildcard type")
          jsig(bounds.hi, existentiallyBound, toplevel, primitiveOK)
        case _ ⇒
          val etp = erasure.erasure(sym0)(tp)
          if (etp eq tp) throw new UnknownSig
          else jsig(etp)
      }
    }
    def toJava(info0: Type): String = {
      val info = info0.dealiasWiden
      info.typeSymbol match {
        case UnitClass    ⇒ if (voidOK) "void" else "scala.runtime.BoxedUnit"
        case NothingClass ⇒ "scala.runtime.Nothing$"
        case BooleanClass ⇒ "boolean"
        case ByteClass    ⇒ "byte"
        case ShortClass   ⇒ "short"
        case CharClass    ⇒ "char"
        case IntClass     ⇒ "int"
        case LongClass    ⇒ "long"
        case FloatClass   ⇒ "float"
        case DoubleClass  ⇒ "double"
        case ArrayClass   ⇒ jsig(info)
        case AnyClass     ⇒ "Object"
        case _ ⇒
          info match {
            case r @ RefinedType(head :: tail, _) ⇒
              fullNameInSig(head.typeSymbol)
            case TypeRef(pre, sym, _) if sym.isAbstractType ⇒
              fullNameInSig(pre.typeSymbol)
            case TypeRef(pre, sym, _) if sym.isValueParameter ⇒
              fullNameInSig(info0.memberType(sym).typeSymbol)
            case _ ⇒
              fullNameInSig(info0.typeSymbol)
          }
      }
    }
    val _info = removeThis(info)
    val result =
      if (needsJavaSig(info)) {
        try jsig(_info, toplevel = true)
        catch { case ex: UnknownSig ⇒ toJava(_info) }
      } else toJava(_info)
    if (result == "scala.Null") "scala.runtime.Null$" else result
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
          case at: AnnotatedType ⇒
            traverse(at.underlying)
          case _ ⇒
            mapOver(tp)
        }
      }
    }
  }

  private def rebindInnerClass(pre: Type, cls: Symbol): Type = {
    if (cls.owner.isClass) cls.owner.tpe else pre // why not cls.isNestedClass?
  }

  private def hiBounds(bounds: TypeBounds): List[Type] = bounds.hi.normalize match {
    case RefinedType(parents, _) ⇒ parents map (_.normalize)
    case tp                      ⇒ tp :: Nil
  }

  import erasure.GenericArray

  private def unboundedGenericArrayLevel(tp: Type): Int = tp match {
    case GenericArray(level, core) if !(core <:< AnyRefClass.tpe) ⇒ level
    case _ ⇒ 0
  }

  private def isTypeParameterInSig(sym: Symbol, initialSymbol: Symbol) = (
    !sym.isHigherOrderTypeParameter &&
    sym.isTypeParameterOrSkolem && (
      (initialSymbol.enclClassChain.exists(sym isNestedIn _)) ||
      (initialSymbol.isMethod && initialSymbol.typeParams.contains(sym))))

  private def needsJavaSig(tp: Type) = !settings.Ynogenericsig.value && NeedsSigCollector.collect(tp)

  class UnknownSig extends Exception

}
