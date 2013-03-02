package akka.rk.buh.is.it;
/**
 * class A
 */
public  class A {
  public  class D extends akka.rk.buh.is.it.A.B {
    public  int i () { throw new RuntimeException(); }
    // not preceding
    // Select(This(newTypeName("D")), newTermName("i "))
    // This(newTypeName("D"))
    public   D () { throw new RuntimeException(); }
  }
  // no position
  public  class D$ {
    public   D$ () { throw new RuntimeException(); }
    public  long math () { throw new RuntimeException(); }
  }
  /**
   * class A.C
   */
  static public  class C1 {
    // no position
    public  class C1$ {
      /**
       * object A.C.C1
       */
      public   C1$ () { throw new RuntimeException(); }
    }
    public   C1 () { throw new RuntimeException(); }
    // not preceding
    // Block(List(Apply(Select(Super(This(newTypeName("C1")), tpnme.EMPTY), nme.CONSTRUCTOR), List())), Literal(Constant(())))
    // Apply(Select(Super(This(newTypeName("C1")), tpnme.EMPTY), nme.CONSTRUCTOR), List())
    // Select(Super(This(newTypeName("C1")), tpnme.EMPTY), nme.CONSTRUCTOR)
    // Super(This(newTypeName("C1")), tpnme.EMPTY)
    // This(newTypeName("C1"))
    // Literal(Constant(()))
    public  akka.rk.buh.is.it.A.C1.C1$ C1 () { throw new RuntimeException(); }
  }
  // no position
  static public  class C1$ {
    /**
     * object C1
     */
    public   C1$ () { throw new RuntimeException(); }
    /**
     * A.C1.method
     */
    public  void method () { throw new RuntimeException(); }
  }
  // no position
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
    /**
     * def x: Int
     */
    public  java.lang.String d (java.lang.String a, akka.rk.buh.is.it.X b) { throw new RuntimeException(); }
  }
  public  class C implements akka.rk.buh.is.it.X {
    public  int i () { throw new RuntimeException(); }
    // not preceding
    // Select(This(newTypeName("C")), newTermName("i "))
    // This(newTypeName("C"))
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
  // not preceding
  // Literal(Constant(0))
  public  akka.rk.buh.is.it.A.D$ D () { throw new RuntimeException(); }
}
