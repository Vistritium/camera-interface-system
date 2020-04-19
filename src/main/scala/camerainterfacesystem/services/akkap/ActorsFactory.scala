package camerainterfacesystem.services.akkap

import akka.actor.typed.scaladsl.ActorContext

trait ActorsFactory {

  def appSupervisor(context: ActorContext[AppSupervisor.Command]): AppSupervisor
  def imageDataService(context: ActorContext[ImageDataService.Command]): ImageDataService

}
