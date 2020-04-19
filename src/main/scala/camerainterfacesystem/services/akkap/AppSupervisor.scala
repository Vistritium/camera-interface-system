package camerainterfacesystem.services.akkap

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import camerainterfacesystem.services.akkap.AppSupervisor.Command
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted

object AppSupervisor {

  sealed trait Command
  final case class GetImageDataService(replyTo: ActorRef[ActorRef[ImageDataService.Command]]) extends Command

}

import com.google.inject.Inject

class AppSupervisor @Inject()(
  @Assisted context: ActorContext[Command],
  actorsFactory: Provider[ActorsFactory],
) extends AbstractBehavior[Command](context) {

  val imageDataService = SpawnUtil.spawnMyDearActor(actorsFactory.get().imageDataService, context)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case AppSupervisor.GetImageDataService(replyTo) => {
        replyTo ! imageDataService
        Behaviors.same
      }
    }
  }
}
