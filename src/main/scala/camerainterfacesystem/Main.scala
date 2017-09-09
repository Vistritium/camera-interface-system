package camerainterfacesystem

import akka.actor.{ActorSystem, Props}
import camerainterfacesystem.db.DB
import camerainterfacesystem.web.WebServer
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging{

  def main(args: Array[String]): Unit = {

    val system = ActorSystem()

    system.actorOf(Props[WebServer], "web")

    logger.info(s"flyway migration: ${DB.migration}")

  }

}
