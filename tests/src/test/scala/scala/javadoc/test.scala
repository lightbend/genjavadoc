/**
 *  Copyright (C) 2009-2011 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.rk
package buh.is.it

import scala.annotation.varargs
import scala.concurrent.duration.FiniteDuration

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
 * Some problematic things: & > =>
 *
 * <p/>
 *
 * {{{
 * and some code<with angle brackets>
 * }}}
 *
 * and an illegal tag:
 * @param x buh
 *
 * @see [[Blarb]]
 * @see [[http://some.url.here]]
 * @see [[https://some.other.url.here]]
 */
class A {
  /**
   * def p(x: Array[Int]): Predef.type
   *
   * @param x an Array
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

  val `public` = 0 // this shall not be emitted
  val `private` = 0 // this shall not be emitted
  val `package` = 0 // this shall not be emitted
  val `static` = 0 // this shall not be emitted
  val `class` = 0 // this shall not be emitted

  val `4711-whatever` = 0 // this shall not be emitted

  /**
   * scala.Nothing should be converted to scala.runtime.Nothing$.
   */
  def nothing(msg: String) = throw new IllegalArgumentException(msg)

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
   * throws
   */
  @throws[IllegalArgumentException]
  @throws(classOf[NullPointerException])
  def testthrows = 0

  /**
   * class A.B
   */
  // one line comment
  class B extends X {
    /**
     * secondary constructor
     */
    def this(stest: String) = this()
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

trait Trait
object Trait {
  private final case class TraitPrivate()
}

/**
 * Privacy is an illusion.
 */
private[it] object PPrivate {
  def method = ()
}

/**
 * Privacy is an illusion.
 */
private object Private {
  def method = ()
}

/**
 * Use protection.
 */
protected[it] object PProtected {
  def method = ()
}

/**
 * Privacy is an illusion.
 */
private[it] trait PTrait {
  def method = ()
  protected final def protectedMethod = ()
}

/**
 * AbstractTypeRef
 */
trait AnAbstractTypeRef {
  type Self <: AnAbstractTypeRef

  def someMethod(): Self = this.asInstanceOf[Self]

  /**
   * And a parameter type ref.
   */
  def otherMethod(t: PTrait, string: String): t.type = t
}

/**
 * The exponentially weighted moving average (EWMA) approach captures short-term
 * movements in volatility for a conditional volatility forecasting model. By virtue
 * of its alpha, or decay factor, this provides a statistical streaming data model
 * that is exponentially biased towards newer entries.
 *
 * http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 *
 * An EWMA only needs the most recent forecast value to be kept, as opposed to a standard
 * moving average model.
 *
 * @param alpha decay factor, sets how quickly the exponential weighting decays for past data compared to new data,
 *   see http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 *
 * @param value the current exponentially weighted moving average, e.g. Y(n - 1), or,
 *             the sampled value resulting from the previous smoothing iteration.
 *             This value is always used as the previous EWMA to calculate the new EWMA.
 *
 */
@SerialVersionUID(1L)
final case class EWMA(value: Double, alpha: Double) {

  require(0.0 <= alpha && alpha <= 1.0, "alpha must be between 0.0 and 1.0")

  /**
   * Calculates the exponentially weighted moving average for a given monitored data set.
   *
   * @param xn the new data point
   * @return a new EWMA with the updated value
   */
  def :+(xn: Double): EWMA = {
    val newValue = (alpha * xn) + (1 - alpha) * value
    if (newValue == value) this // no change
    else copy(value = newValue)
  }

}
object EWMA {

  /**
   * math.log(2)
   */
  private val LogOf2 = 0.69315

  /**
   * Calculate the alpha (decay factor) used in [[akka.cluster.EWMA]]
   * from specified half-life and interval between observations.
   * Half-life is the interval over which the weights decrease by a factor of two.
   * The relevance of each data sample is halved for every passing half-life duration,
   * i.e. after 4 times the half-life, a data sample’s relevance is reduced to 6% of
   * its original relevance. The initial relevance of a data sample is given by
   * 1 – 0.5 ^ (collect-interval / half-life).
   */
  def alpha(halfLife: FiniteDuration, collectInterval: FiniteDuration): Double = {
    val halfLifeMillis = halfLife.toMillis
    require(halfLife.toMillis > 0, "halfLife must be > 0 s")
    val decayRate = LogOf2 / halfLifeMillis
    1 - math.exp(-decayRate * collectInterval.toMillis)
  }
}
