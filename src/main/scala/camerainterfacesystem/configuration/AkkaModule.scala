package camerainterfacesystem.configuration


import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props}
import akka.{actor => classic}
import camerainterfacesystem.services.AkkaRefNames
import camerainterfacesystem.services.akkap.{ActorsFactory, AppSupervisor, ImageDataService, SpawnUtil}
import com.google.inject.name.Named
import com.google.inject.{Injector, Module, Provider, Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration._

@Configuration
class AkkaModule extends ScalaModule with LazyLogging {

  override def configure(): Unit = {
    install(new FactoryModuleBuilder().build(classOf[ActorsFactory]))
  }

  private implicit val timeout: Timeout = 3.seconds

  @Provides
  @Singleton
  @Named(AkkaRefNames.ImageDataService)
  def imageDataService(
    @Named(AkkaRefNames.TypedSupervisor) supervisor: ActorRef[AppSupervisor.Command],
    classicSystem: classic.ActorSystem
  ): ActorRef[ImageDataService.Command] = {
    Await.result(supervisor.ask(AppSupervisor.GetImageDataService)(timeout, classicSystem.scheduler.toTyped), 3.seconds)
  }

  @Provides
  @Singleton
  @Named(AkkaRefNames.TypedSupervisor)
  def typedSystem(actorsFactory: Provider[ActorsFactory], classicSystem: classic.ActorSystem): ActorRef[AppSupervisor.Command] = {
    SpawnUtil.spawnMyDearActor(actorsFactory.get().appSupervisor, classicSystem)
  }



  @Provides
  @Singleton
  def classicSystem(): classic.ActorSystem = {
    classic.ActorSystem("main")
  }

}
