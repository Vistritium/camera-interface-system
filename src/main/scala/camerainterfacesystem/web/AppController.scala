package camerainterfacesystem.web

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.util.Timeout
import camerainterfacesystem.Main
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Controller
abstract class AppController extends LazyLogging with ControllerUtils {

  protected val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  protected implicit val timeout: Timeout = Timeout(1 hour)
  protected implicit val executionContext: ExecutionContext = Main.system.dispatcher

  def route: Route

  def handleFutureError[T](dir: Directive1[Try[T]])(next: T => Route): Route = {
    dir {
      case Failure(exception) => {
        logger.debug("futureError", exception)
        complete(HttpResponse(status = StatusCodes.InternalServerError, entity = s"${exception.getMessage}"))
      }
      case Success(value) => next(value)
    }
  }

}
