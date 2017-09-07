package camerainterfacesystem

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

abstract class AppActor extends Actor with LazyLogging {
  protected implicit val dispatcher: ExecutionContextExecutor = context.dispatcher

}
