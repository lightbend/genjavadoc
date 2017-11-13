package akka.rk.buh.is.it;
public  class ClassWithInner {
  public  class Foo {
    static public  int companionObjectValue ()  { throw new RuntimeException(); }
    public   Foo ()  { throw new RuntimeException(); }
    public  int methodOfFoo (int i)  { throw new RuntimeException(); }
  }
  public  class Foo$ {
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final Foo$ MODULE$ = null;
    public   Foo$ ()  { throw new RuntimeException(); }
    public  int companionObjectValue ()  { throw new RuntimeException(); }
  }
  public   ClassWithInner ()  { throw new RuntimeException(); }
  public  akka.rk.buh.is.it.ClassWithInner.Foo$ Foo ()  { throw new RuntimeException(); }
}