package camerainterfacesystem.services

import akka.util.Timeout
import camerainterfacesystem.Main
import com.typesafe.scalalogging.LazyLogging

import concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

trait AppService extends LazyLogging {
  protected implicit val timeout: Timeout = Timeout(1 hour)
  protected implicit val executionContext: ExecutionContextExecutor = Main.system.dispatcher
}
