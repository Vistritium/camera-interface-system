package camerainterfacesystem

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{OneForOneStrategy, SupervisorStrategyConfigurator}

class GuardianSupervisionStrategy extends SupervisorStrategyConfigurator {
  override def create() = OneForOneStrategy() {
    case e => Escalate
  }
}
