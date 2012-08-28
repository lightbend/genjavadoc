/**
 *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.rk
package buh.is.it

/**
 * class A
 */
class A {
  /**
   * def p(x: Array[Int]): Predef.type
   */
  def p(x: Array[Int]) = Predef

  /**
   * def params[T <: B](b: T): T
   */
  def params[T <: B](b: T): T = b

  /**
   * extra comment
   */

  /**
   * class A.B
   */
  // one line comment
  class B extends A {
    /**
     * secondary constructor
     */
    def this(s: String) = this()
    /**
     * def b(args: String*): Unit
     */
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
   * val static: String
   */
  val static = "1"
  /**
   * static p
   */
  def p = "i am not forwarded"
    
  object NoComment
  
  /**
   * a non-comment
   */
  p
  val x = new AnyRef
}
