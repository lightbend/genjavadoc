/**
 *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.rk
package buh.is.it

import scala.annotation.varargs

trait Y[A] {
  // def pi = 3.14
}
trait X extends Serializable with Y[A]

abstract class Z {
  def pi: Double
}

/**
 * I am an object. I have no class.
 */
object Blarb {
  sealed trait Fuz
  case object A extends Fuz
  case class B(x: String) extends Fuz
}

/**
 * class A
 * 
 * with a `second` paragraph
 * 
 * and a third one `with
 * a break` in some code.
 * 
 * {{{
 * and some code<with angle brackets>
 * }}}
 */
class A {
  /**
   * def p(x: Array[Int]): Predef.type
   */
  def p(x: Array[Int]) = Predef

  /**
   * def params[T <: B](b: T): T
   */
  def params[T >: D <: B](b: T): List[T] = b :: Nil

  /**
   * def map
   */
  def map = Map(1 -> "1")

  def default = 0 // this shall not be emitted
  def goto = 0 // this shall not be emitted
  def interface = 0 // this shall not be emitted
  def switch = 0 // this shall not be emitted
  
  /**
   * Unitparam
   */
  def unitParam(unit: Unit) = 42
  
  /**
   * mangledNames
   */
  def mangledNames(default: String, goto: String, interface: String) = 12

  /**
   * blarb
   */
  def blarb: Blarb.Fuz = Blarb.A

  /**
   * refined
   */
  def refined: Z with Y[Unit] = null

  /**
   * poly
   */
  def poly[A, M[A] <: Y[A]] = 42

  /**
   * extra comment
   */
  
  /**
   * varargs
   */
  @varargs
  def hello(s: String*) = 0

  /**
   * class A.B
   */
  // one line comment
  class B extends X {
    /**
     * secondary constructor
     */
    def this(s: String) = this()
    /**
     * def b(args: java.lang.String*): Unit
     */
    @varargs
    def b(args: String*) {
      /**
       * new AnyRef {}
       */
      new AnyRef {
        /**
         * def x: Int
         */
        def x = 2
      }
    }
    def d(a: String)(b: X) = ""
  }

  class C extends { val i = 1 } with X
  /**
   * class A.D
   */
  class D extends { /** val i */ val i = 1 } with B
  /**
   * object A.D
   */
  object D {
    /**
     * def A.D.math
     */
    def math = 0l
  }
}

/**
 * object A
 */
object A {
  /**
   * def IAmStatic: Int
   */
  def IAmStatic = 42
  /**
   * val stattic: java.lang.String
   */
  val stattic = "1"
  /**
   * static p
   */
  def p = "i am not forwarded"

  /**
   * class A.C
   */
  class C1 {
    /**
     * object A.C.C1
     */
    object C1
  }

  /**
   * object C1
   */
  object C1 {
    /**
     * A.C1.method
     */
    def method = ()
  }

  /**
   * object A.NoComment
   */
  private object NoComment

  /**
   * a non-comment
   */
  p
  val x = new AnyRef
}
