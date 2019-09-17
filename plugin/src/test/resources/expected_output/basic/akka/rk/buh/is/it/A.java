package akka.rk.buh.is.it;
/**
 * class A
 * <p>
 * with a <code>second</code> paragraph
 * <p>
 * and a third one <code>with
 * a break</code> in some code.
 * <p>
 * Some problematic things: &amp; > =&gt;
 * <p>
 * <p></p>
 * <p>
 * <pre><code>
 * and some code&lt;with angle brackets&gt;
 * </code></pre>
 * <p>
 * and an illegal tag:
 * param:  x buh
 * <p>
 * @see Blarb
 * @see <a href="http://some.url.here"/>
 * @see <a href="https://some.other.url.here"/>
 */
public  class A {
  /**
   * class A.D
   */
  public  class D extends akka.rk.buh.is.it.A.B {
    /** val i */
    public  int i ()  { throw new RuntimeException(); }
    // not preceding
    public   D ()  { throw new RuntimeException(); }
  }
  /**
   * object A.D
   */
  public  class D$ implements scala.Serializable {
    /**
     * And a nested object.
     */
    public  class E$ {
      public   E$ ()  { throw new RuntimeException(); }
    }
    /**
     * A nested non-static class.
     */
    public  class NonStatic {
      public   NonStatic ()  { throw new RuntimeException(); }
    }
    public   D$ ()  { throw new RuntimeException(); }
    /**
     * def A.D.math
     * @return (undocumented)
     */
    public  long math ()  { throw new RuntimeException(); }
    public  akka.rk.buh.is.it.A.D$.E$ E ()  { throw new RuntimeException(); }
  }
  /**
   * class A.B
   */
  public  class B implements akka.rk.buh.is.it.X {
    // not preceding
    public   B ()  { throw new RuntimeException(); }
    /**
     * secondary constructor
     * @param stest (undocumented)
     */
    public   B (java.lang.String stest)  { throw new RuntimeException(); }
    /**
     * def b(args: java.lang.String*): Unit
     * @param args (undocumented)
     */
    public  void b (java.lang.String... args)  { throw new RuntimeException(); }
    /**
     * def b(args: java.lang.String*): Unit
     * @param args (undocumented)
     */
    public  void b (scala.collection.Seq<java.lang.String> args)  { throw new RuntimeException(); }
    public  java.lang.String d (java.lang.String a, akka.rk.buh.is.it.X b)  { throw new RuntimeException(); }
  }
  public  class C implements akka.rk.buh.is.it.X {
    // not preceding
    public   C ()  { throw new RuntimeException(); }
    public  int i ()  { throw new RuntimeException(); }
  }
  /** tailrecced */
  public final class TR {
    public   TR ()  { throw new RuntimeException(); }
    public  void tr (java.lang.String r)  { throw new RuntimeException(); }
  }
  /**
   * object A.C.C1
   */
  static public  class C1$C1$ {
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final C1$C1$ MODULE$ = null;
    public   C1$C1$ ()  { throw new RuntimeException(); }
  }
  /**
   * class A.C
   */
  static public  class C1 {
    public   C1 ()  { throw new RuntimeException(); }
    public  akka.rk.buh.is.it.A.C1$C1$ C1 ()  { throw new RuntimeException(); }
  }
  /**
   * object C1
   */
  static public  class C1$ {
    /**
     * And another nested object.
     */
    static public  class EE$ {
      /**
       * Static reference to the singleton instance of this Scala object.
       */
      public static final EE$ MODULE$ = null;
      public   EE$ ()  { throw new RuntimeException(); }
    }
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final C1$ MODULE$ = null;
    public   C1$ ()  { throw new RuntimeException(); }
    /**
     * A.C1.method
     */
    public  void method ()  { throw new RuntimeException(); }
  }
  /**
   * def IAmStatic: Int
   * @return (undocumented)
   */
  static public  int IAmStatic ()  { throw new RuntimeException(); }
  /**
   * val stattic: java.lang.String
   * @return (undocumented)
   */
  static public  java.lang.String stattic ()  { throw new RuntimeException(); }
  static public  java.lang.Object x ()  { throw new RuntimeException(); }
  /**
   * varargs
   * @param s (undocumented)
   * @return (undocumented)
   */
  public  int hello (java.lang.String... s)  { throw new RuntimeException(); }
  // not preceding
  public   A ()  { throw new RuntimeException(); }
  /**
   * def p(x: Array[Int]): Predef.type
   * <p>
   * @param x an Array
   * @return (undocumented)
   */
  public  scala.Predef$ p (int[] x)  { throw new RuntimeException(); }
  /**
   * def params[T <: B](b: T): T
   * @param b (undocumented)
   * @return (undocumented)
   */
  public <T extends akka.rk.buh.is.it.A.B> scala.collection.immutable.List<T> params (T b)  { throw new RuntimeException(); }
  /**
   * def map
   * @return (undocumented)
   */
  public  scala.collection.immutable.Map<java.lang.Object, java.lang.String> map ()  { throw new RuntimeException(); }
  /**
   * scala.Nothing should be converted to scala.runtime.Nothing$.
   * @param msg (undocumented)
   * @return (undocumented)
   */
  public  scala.runtime.Nothing$ nothing (java.lang.String msg)  { throw new RuntimeException(); }
  /**
   * Unitparam
   * @param unit (undocumented)
   * @return (undocumented)
   */
  public  int unitParam (scala.runtime.BoxedUnit unit)  { throw new RuntimeException(); }
  /**
   * mangledNames
   * @param default_ (undocumented)
   * @param goto_ (undocumented)
   * @param interface_ (undocumented)
   * @return (undocumented)
   */
  public  int mangledNames (java.lang.String default_, java.lang.String goto_, java.lang.String interface_)  { throw new RuntimeException(); }
  /**
   * blarb
   * @return (undocumented)
   */
  public  akka.rk.buh.is.it.Blarb.Fuz blarb ()  { throw new RuntimeException(); }
  /**
   * refined
   * @return (undocumented)
   */
  public  akka.rk.buh.is.it.Z refined ()  { throw new RuntimeException(); }
  /**
   * poly
   * @return (undocumented)
   */
  public <A extends java.lang.Object, M extends akka.rk.buh.is.it.Y<java.lang.Object>> int poly ()  { throw new RuntimeException(); }
  /**
   * varargs
   * @param s (undocumented)
   * @return (undocumented)
   */
  public  int hello (scala.collection.Seq<java.lang.String> s)  { throw new RuntimeException(); }
  /**
   * throws
   * @return (undocumented)
   */
  public  int testthrows () throws java.lang.IllegalArgumentException, java.lang.NullPointerException { throw new RuntimeException(); }
  public  scala.runtime.Null$ getNull ()  { throw new RuntimeException(); }
  /**
   * Accessor for nested Scala object
   * @return (undocumented)
   */
  public  akka.rk.buh.is.it.A.D$ D ()  { throw new RuntimeException(); }
}
