package camerainterfacesystem

import akka.actor.{ActorRef, ActorSystem, Props}
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.DB
import camerainterfacesystem.services.ImageDataService
import camerainterfacesystem.web.WebServer
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  val system = ActorSystem()
  Azure.init()

  val web: ActorRef = system.actorOf(Props[WebServer], "web")
  val imageDataService: ActorRef = system.actorOf(Props[ImageDataService], "imagedataservice")

  def main(args: Array[String]): Unit = {
    logger.info(s"flyway migration: ${DB.migration}")
    fail
  }

}
