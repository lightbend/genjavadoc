package akka.persistence.testkit;
public  interface ProcessingPolicy<U extends java.lang.Object> {
  static public  interface DefaultPolicies<U extends java.lang.Object> {
    public  class PassAll {
      static public  java.lang.String productPrefix ()  { throw new RuntimeException(); }
      static public  int productArity ()  { throw new RuntimeException(); }
      static public  Object productElement (int x$1)  { throw new RuntimeException(); }
      static public  scala.collection.Iterator<java.lang.Object> productIterator ()  { throw new RuntimeException(); }
      static public  boolean canEqual (Object x$1)  { throw new RuntimeException(); }
      static public  int hashCode ()  { throw new RuntimeException(); }
      static public  java.lang.String toString ()  { throw new RuntimeException(); }
      static public abstract  boolean equals (Object that)  ;
    }
    public  class PassAll$ implements akka.persistence.testkit.ProcessingPolicy<U>, scala.Product, scala.Serializable {
      /**
        * Static reference to the singleton instance of this Scala object.
        */
      public static final PassAll$ MODULE$ = null;
      public   PassAll$ ()  { throw new RuntimeException(); }
      public  java.lang.String productPrefix ()  { throw new RuntimeException(); }
      public  int productArity ()  { throw new RuntimeException(); }
      public  Object productElement (int x$1)  { throw new RuntimeException(); }
      public  scala.collection.Iterator<java.lang.Object> productIterator ()  { throw new RuntimeException(); }
      public  boolean canEqual (Object x$1)  { throw new RuntimeException(); }
      public  int hashCode ()  { throw new RuntimeException(); }
      public  java.lang.String toString ()  { throw new RuntimeException(); }
    }
    public  akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<U>.PassAll$ PassAll ()  ;
  }
}
