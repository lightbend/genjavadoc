package akka.rk.buh.is.it;
/**
 * <p>
 * class A</p>
 */
public  class A {
  /**
   * <p>
   * class A.D</p>
   */
  public  class D extends akka.rk.buh.is.it.A.B {
    /** val i */
    public  int i () { throw new RuntimeException(); }
    // not preceding
    public   D () { throw new RuntimeException(); }
  }
  // no position
  /**
   * <p>
   * object A.D</p>
   */
  public  class D$ implements scala.Serializable {
    public   D$ () { throw new RuntimeException(); }
    /**
     * <p>
     * def A.D.math</p>
     */
    public  long math () { throw new RuntimeException(); }
    // not preceding
    private  java.lang.Object readResolve () { throw new RuntimeException(); }
  }
  /**
   * <p>
   * class A.C</p>
   */
  static public  class C1 {
    // no position
    /**
     * <p>
     * object A.C.C1</p>
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
   * <p>
   * object C1</p>
   */
  static public  class C1$ {
    public   C1$ () { throw new RuntimeException(); }
    /**
     * <p>
     * A.C1.method</p>
     */
    public  void method () { throw new RuntimeException(); }
  }
  // no position
  /**
   * <p>
   * object A.NoComment</p>
   */
  static private  class NoComment$ {
    public   NoComment$ () { throw new RuntimeException(); }
  }
  /**
   * <p>
   * class A.B</p>
   */
  public  class B implements akka.rk.buh.is.it.X {
    public   B () { throw new RuntimeException(); }
    /**
     * <p>
     * secondary constructor</p>
     */
    public   B (java.lang.String s) { throw new RuntimeException(); }
    /**
     * <p>
     * def b(args: java.lang.String*): Unit</p>
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
   * <p>
   * def IAmStatic: Int</p>
   */
  static public  int IAmStatic () { throw new RuntimeException(); }
  /**
   * <p>
   * val stattic: java.lang.String</p>
   */
  static public  java.lang.String stattic () { throw new RuntimeException(); }
  static public  java.lang.Object x () { throw new RuntimeException(); }
  public   A () { throw new RuntimeException(); }
  /**
   * <p>
   * def p(x: Array[Int]): Predef.type</p>
   */
  public  scala.Predef$ p (int[] x) { throw new RuntimeException(); }
  /**
   * <p>
   * def params[T <: B](b: T): T</p>
   */
  public <T extends akka.rk.buh.is.it.A.B> scala.collection.immutable.List<T> params (T b) { throw new RuntimeException(); }
  /**
   * <p>
   * def map</p>
   */
  public  scala.collection.immutable.Map<java.lang.Object, java.lang.String> map () { throw new RuntimeException(); }
  /**
   * <p>
   * Unitparam</p>
   */
  public  int unitParam (scala.runtime.BoxedUnit unit) { throw new RuntimeException(); }
  /**
   * <p>
   * mangledNames</p>
   */
  public  int mangledNames (java.lang.String default_, java.lang.String goto_, java.lang.String interface_) { throw new RuntimeException(); }
  /**
   * <p>
   * blarb</p>
   */
  public  akka.rk.buh.is.it.Blarb.Fuz blarb () { throw new RuntimeException(); }
  /**
   * <p>
   * refined</p>
   */
  public  akka.rk.buh.is.it.Z refined () { throw new RuntimeException(); }
  /**
   * <p>
   * poly</p>
   */
  public <A extends java.lang.Object, M extends akka.rk.buh.is.it.Y<java.lang.Object>> int poly () { throw new RuntimeException(); }
  /**
   * Accessor for nested Scala object
   */
  public  akka.rk.buh.is.it.A.D$ D () { throw new RuntimeException(); }
}
