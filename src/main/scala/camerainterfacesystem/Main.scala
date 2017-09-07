package camerainterfacesystem

import akka.actor.{ActorSystem, Props}
import camerainterfacesystem.web.WebServer

object Main {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem()

    system.actorOf(Props[WebServer], "web")

  }

}
