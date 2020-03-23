package akka.persistence.testkit;
public  interface EventStorage {
  static public  class JournalPolicies$ implements akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation> {
    /**
      * Static reference to the singleton instance of this Scala object.
      */
    public static final JournalPolicies$ MODULE$ = null;
    public  akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation>.PassAll$ PassAll ()  { throw new RuntimeException(); }
    public   JournalPolicies$ ()  { throw new RuntimeException(); }
  }
  public  void akka$persistence$testkit$EventStorage$_setter_$DefaultPolicy_$eq (akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation>.PassAll x$1)  ;
  public  akka.persistence.testkit.ProcessingPolicy.DefaultPolicies<akka.persistence.testkit.JournalOperation>.PassAll DefaultPolicy ()  ;
}