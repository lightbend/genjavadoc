package akka.rk.buh.is.it;
public  class CompressionProtocol {
  static public  class Events$ {
    /**
     * Static reference to the singleton instance of this Scala object.
     */
    public static final Events$ MODULE$ = null;
    public final class HeavyHitterDetected implements akka.rk.buh.is.it.CompressionProtocol.Events.Event, scala.Product, scala.Serializable {
      static public  akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected apply (Object key, int id, long count)  { throw new RuntimeException(); }
      static public  scala.Option<scala.Tuple3<java.lang.Object, java.lang.Object, java.lang.Object>> unapply (akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected x$0)  { throw new RuntimeException(); }
      public  Object key ()  { throw new RuntimeException(); }
      public  int id ()  { throw new RuntimeException(); }
      public  long count ()  { throw new RuntimeException(); }
      // not preceding
      public   HeavyHitterDetected (Object key, int id, long count)  { throw new RuntimeException(); }
      public  akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected copy (Object key, int id, long count)  { throw new RuntimeException(); }
      // not preceding
      public  Object copy$default$1 ()  { throw new RuntimeException(); }
      public  int copy$default$2 ()  { throw new RuntimeException(); }
      public  long copy$default$3 ()  { throw new RuntimeException(); }
      // not preceding
      public  java.lang.String productPrefix ()  { throw new RuntimeException(); }
      public  int productArity ()  { throw new RuntimeException(); }
      public  Object productElement (int x$1)  { throw new RuntimeException(); }
      public  scala.collection.Iterator<java.lang.Object> productIterator ()  { throw new RuntimeException(); }
      public  boolean canEqual (Object x$1)  { throw new RuntimeException(); }
      public  int hashCode ()  { throw new RuntimeException(); }
      public  java.lang.String toString ()  { throw new RuntimeException(); }
      public  boolean equals (Object x$1)  { throw new RuntimeException(); }
    }
    public  class HeavyHitterDetected$ extends scala.runtime.AbstractFunction3<java.lang.Object, java.lang.Object, java.lang.Object, akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected> implements scala.Serializable {
      /**
       * Static reference to the singleton instance of this Scala object.
       */
      public static final HeavyHitterDetected$ MODULE$ = null;
      public   HeavyHitterDetected$ ()  { throw new RuntimeException(); }
      public final  java.lang.String toString ()  { throw new RuntimeException(); }
      public  akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected apply (Object key, int id, long count)  { throw new RuntimeException(); }
      public  scala.Option<scala.Tuple3<java.lang.Object, java.lang.Object, java.lang.Object>> unapply (akka.rk.buh.is.it.CompressionProtocol.Events.HeavyHitterDetected x$0)  { throw new RuntimeException(); }
      private  java.lang.Object readResolve ()  { throw new RuntimeException(); }
    }
    public  interface Event {
    }
    public   Events$ ()  { throw new RuntimeException(); }
  }
}
