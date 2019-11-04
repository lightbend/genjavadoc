package akka.actor.typed

abstract class Props private[akka] () extends Product with Serializable {
  private[akka] def next: Props
}

sealed abstract class DispatcherSelector extends Props

object DispatcherSelector