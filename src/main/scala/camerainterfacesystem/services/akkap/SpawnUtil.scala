package camerainterfacesystem.services.akkap

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.{actor => classic}

object SpawnUtil {

  type Spawner[T] = (Behavior[T], String) => ActorRef[T]

  def spawnMyDearActor[T](factory: ActorContext[T] => Behavior[T], spawner: ActorContext[_])(implicit m: Manifest[T]): ActorRef[T] = {
    val beh = Behaviors.setup[T](ctx => factory.apply(ctx))
    spawner.spawn(beh, m.runtimeClass.getSimpleName)
  }

  def spawnMyDearActor[T](factory: ActorContext[T] => Behavior[T], spawner: classic.ActorSystem)(implicit m: Manifest[T]): ActorRef[T] = {
    val beh = Behaviors.setup[T](ctx => factory.apply(ctx))
    spawner.spawn(beh, m.runtimeClass.getSimpleName)
  }

}
