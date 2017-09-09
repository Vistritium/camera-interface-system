package camerainterfacesystem

import akka.actor.{ActorSystem, Props}
import camerainterfacesystem.db.DB
import camerainterfacesystem.web.WebServer
import camerainterfacesystem.web.controllers.upload.AzureUploader
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging{

  def main(args: Array[String]): Unit = {

    AzureUploader.init()

    val system = ActorSystem()

    system.actorOf(Props[WebServer], "web")

    logger.info(s"flyway migration: ${DB.migration}")

  }

}
