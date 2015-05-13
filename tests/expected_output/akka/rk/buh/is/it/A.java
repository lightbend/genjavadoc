package akka.rk.buh.is.it;
/**
 * class A
 * <p>
 * with a <code>second</code> paragraph
 * <p>
 * and a third one <code>with
 * a break</code> in some code.
 * <p>
 * Some problematic things: &amp;
 * <p>
 * <p></p>
 * <p>
 * <pre><code>
 * and some code&lt;with angle brackets&gt;
 * </code></pre>
 * <p>
 * and an illegal tag:
 * param: x buh
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
    public  int i () { throw new RuntimeException(); }
    // not preceding
    public   D () { throw new RuntimeException(); }
  }
  // no position
  /**
   * object A.D
   */
  public  class D$ implements scala.Serializable {
    public   D$ () { throw new RuntimeException(); }
    /**
     * def A.D.math
     */
    public  long math () { throw new RuntimeException(); }
    // not preceding
    private  java.lang.Object readResolve () { throw new RuntimeException(); }
  }
  /**
   * class A.C
   */
  static public  class C1 {
    // no position
    /**
     * object A.C.C1
     */
    public  class C1$ {
      public   C1$ () { throw new RuntimeException(); }
    }
    public   C1 () { throw new RuntimeException(); }
    /**
     * Accessor for nested Scala object
     */
    public  akka.rk.buh.is.it.A.C1.C1$ C1 () { throw new RuntimeException(); }
  }
  // no position
  /**
   * object C1
   */
  static public  class C1$ {
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final C1$ MODULE$ = null;
    public   C1$ () { throw new RuntimeException(); }
    /**
     * A.C1.method
     */
    public  void method () { throw new RuntimeException(); }
  }
  // no position
  /**
   * object A.NoComment
   */
  static private  class NoComment$ {
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final NoComment$ MODULE$ = null;
    public   NoComment$ () { throw new RuntimeException(); }
  }
  /**
   * class A.B
   */
  public  class B implements akka.rk.buh.is.it.X {
    /**
     * def b(args: java.lang.String*): Unit
     */
    public  void b (java.lang.String... args) { throw new RuntimeException(); }
    // not preceding
    public   B () { throw new RuntimeException(); }
    /**
     * secondary constructor
     */
    public   B (java.lang.String s) { throw new RuntimeException(); }
    /**
     * def b(args: java.lang.String*): Unit
     */
    public  void b (scala.collection.Seq<java.lang.String> args) { throw new RuntimeException(); }
    public  java.lang.String d (java.lang.String a, akka.rk.buh.is.it.X b) { throw new RuntimeException(); }
  }
  public  class C implements akka.rk.buh.is.it.X {
    public  int i () { throw new RuntimeException(); }
    // not preceding
    public   C () { throw new RuntimeException(); }
  }
  /**
   * def IAmStatic: Int
   */
  static public  int IAmStatic () { throw new RuntimeException(); }
  /**
   * val stattic: java.lang.String
   */
  static public  java.lang.String stattic () { throw new RuntimeException(); }
  static public  java.lang.Object x () { throw new RuntimeException(); }
  /**
   * varargs
   */
  public  int hello (java.lang.String... s) { throw new RuntimeException(); }
  // not preceding
  public   A () { throw new RuntimeException(); }
  /**
   * def p(x: Array[Int]): Predef.type
   * <p>
   * @param x an Array
   */
  public  scala.Predef$ p (int[] x) { throw new RuntimeException(); }
  /**
   * def params[T <: B](b: T): T
   */
  public <T extends akka.rk.buh.is.it.A.B> scala.collection.immutable.List<T> params (T b) { throw new RuntimeException(); }
  /**
   * def map
   */
  public  scala.collection.immutable.Map<java.lang.Object, java.lang.String> map () { throw new RuntimeException(); }
  /**
   * scala.Nothing should be converted to scala.runtime.Nothing$.
   */
  public  scala.runtime.Nothing$ nothing (java.lang.String msg) { throw new RuntimeException(); }
  /**
   * Unitparam
   */
  public  int unitParam (scala.runtime.BoxedUnit unit) { throw new RuntimeException(); }
  /**
   * mangledNames
   */
  public  int mangledNames (java.lang.String default_, java.lang.String goto_, java.lang.String interface_) { throw new RuntimeException(); }
  /**
   * blarb
   */
  public  akka.rk.buh.is.it.Blarb.Fuz blarb () { throw new RuntimeException(); }
  /**
   * refined
   */
  public  akka.rk.buh.is.it.Z refined () { throw new RuntimeException(); }
  /**
   * poly
   */
  public <A extends java.lang.Object, M extends akka.rk.buh.is.it.Y<java.lang.Object>> int poly () { throw new RuntimeException(); }
  /**
   * varargs
   */
  public  int hello (scala.collection.Seq<java.lang.String> s) { throw new RuntimeException(); }
  /**
   * Accessor for nested Scala object
   */
  public  akka.rk.buh.is.it.A.D$ D () { throw new RuntimeException(); }
}
