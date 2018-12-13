package akka.actor.dsl

trait Inbox {
  protected trait InboxExtension {
    val DSLInboxQueueSize = 42
  }
}
