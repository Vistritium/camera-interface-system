package camerainterfacesystem.services

import akka.actor.ActorSystem
import akka.actor.typed.Scheduler
import akka.actor.typed.scaladsl.adapter._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

trait AppService extends LazyLogging {
  protected implicit val timeout: Timeout = Timeout(1.hour)
  protected val system: ActorSystem
  protected implicit val scheduler: Scheduler = system.scheduler.toTyped
  protected implicit val context = system.dispatcher
}
