package camerainterfacesystem

import akka.actor.{ActorSystem, Props}
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.DB
import camerainterfacesystem.services.akkap.ImageDataService
import camerainterfacesystem.web.WebServer
import com.google.inject.{Inject, Injector, Singleton}
import com.typesafe.scalalogging.LazyLogging

@Singleton
class Starter @Inject()(
  azure: Azure,
  webServer: WebServer,
  system: ActorSystem,
  db: DB,
  injector: Injector
) extends LazyLogging {

  def start(): Unit = {

    webServer.start()
    azure.init()
    logger.info(db.migration.toString)

  }

}
