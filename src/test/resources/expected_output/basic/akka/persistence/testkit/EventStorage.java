package akka.persistence.testkit;
public  interface EventStorage {
  static public  class JournalPolicies$ implements akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation> {
    /**
      * Static reference to the singleton instance of this Scala object.
      */
    public static final JournalPolicies$ MODULE$ = null;
    public   JournalPolicies$ ()  { throw new RuntimeException(); }
  }
  public  akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation>.PassAll$ DefaultPolicy ()  ;
}
