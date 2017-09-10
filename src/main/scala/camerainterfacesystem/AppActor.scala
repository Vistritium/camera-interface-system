package camerainterfacesystem

import akka.actor.{Actor, ActorRef, Status}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

abstract class AppActor extends Actor with LazyLogging {
  protected implicit val dispatcher: ExecutionContextExecutor = context.dispatcher

  def replyAsk[T](sender: ActorRef, fut: Future[T]): Unit = {
    fut onComplete {
      case Failure(exception) => sender ! Status.Failure(exception)
      case Success(value) => sender ! value
    }
  }

}
