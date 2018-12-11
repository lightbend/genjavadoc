package akka.actor.typed;
public abstract class DispatcherSelector extends akka.actor.typed.Props {
  static private  java.lang.Object readResolve ()  { throw new RuntimeException(); }
  static public abstract  boolean canEqual (Object that)  ;
  static public abstract  boolean equals (Object that)  ;
  static public abstract  Object productElement (int n)  ;
  static public abstract  int productArity ()  ;
  static public  scala.collection.Iterator<java.lang.Object> productIterator ()  { throw new RuntimeException(); }
  static public  java.lang.String productPrefix ()  { throw new RuntimeException(); }
  static public  java.lang.String productElementName (int n)  { throw new RuntimeException(); }
  static public  scala.collection.Iterator<java.lang.String> productElementNames ()  { throw new RuntimeException(); }
  static public abstract  akka.actor.typed.Props next ()  ;
  public   DispatcherSelector ()  { throw new RuntimeException(); }
}
