package camerainterfacesystem.utils

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{AllForOneStrategy, Props, SupervisorStrategy, Terminated}
import camerainterfacesystem.AppActor

class StoppingSupervisor(props: Props) extends AppActor {

  private val child = context.actorOf(props)
  context.watch(child)

  override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
    case e: Exception => {
      logger.warn(s"Actor stopped unexpectedly: ${e.getMessage}")
      logger.debug("Actor stopped unexpectedly", e)
      Stop
    }
  }

  override def receive: Receive = {
    case Terminated(terminated) if terminated == child => {
      context.stop(self)
    }
    case msg => if (sender() == child) {
      context.parent forward msg
    } else {
      child forward msg
    }
  }

}

object StoppingSupervisor {

  def apply(child: Props) = Props(classOf[StoppingSupervisor], child)
}
