package akka.rk.buh.is.it;
/**
 * class A
 */
public  class A {
  /**
   * class A.D
   */
  public  class D extends akka.rk.buh.is.it.A.B {
    /** val i */
    public  int i () { throw new RuntimeException(); }
    public   D () { throw new RuntimeException(); }
  }
  /**
   * object A.D
   */
  public  class D$ implements scala.Serializable {
    public   D$ () { throw new RuntimeException(); }
    /**
     * def A.D.math
     */
    public  long math () { throw new RuntimeException(); }
    private  java.lang.Object readResolve () { throw new RuntimeException(); }
  }
  /**
   * class A.C
   */
  static public  class C1 {
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
  /**
   * object C1
   */
  static public  class C1$ {
    public   C1$ () { throw new RuntimeException(); }
    /**
     * A.C1.method
     */
    public  void method () { throw new RuntimeException(); }
  }
  /**
   * object A.NoComment
   */
  static private  class NoComment$ {
    public   NoComment$ () { throw new RuntimeException(); }
  }
  /**
   * class A.B
   */
  public  class B implements akka.rk.buh.is.it.X {
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
  public   A () { throw new RuntimeException(); }
  /**
   * def p(x: Array[Int]): Predef.type
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
   * Accessor for nested Scala object
   */
  public  akka.rk.buh.is.it.A.D$ D () { throw new RuntimeException(); }
}
