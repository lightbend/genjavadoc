package akka.persistence.testkit

import akka.persistence.testkit.ProcessingPolicy.DefaultPolicies

private[testkit] trait EventStorage {
    import EventStorage._

    val DefaultPolicy = JournalPolicies.PassAll
}

object EventStorage {
    object JournalPolicies extends DefaultPolicies[JournalOperation]
}

sealed trait JournalOperation