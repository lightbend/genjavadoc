package akka.persistence.testkit

trait ProcessingPolicy[U]

object ProcessingPolicy {
    private[testkit] trait DefaultPolicies[U] {
        type PolicyType = ProcessingPolicy[U]

        case object PassAll extends PolicyType
    }
}