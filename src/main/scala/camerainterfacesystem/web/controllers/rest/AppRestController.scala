package camerainterfacesystem.web.controllers.rest

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import camerainterfacesystem.web.AppController
import com.fasterxml.jackson.databind.ObjectMapper

import scala.concurrent.Future

abstract class AppRestController extends AppController {

  protected val objectMapper: ObjectMapper

  override def route: Route = pathPrefix("api") {
    restRoute
  }

  protected def restComplete(result: Any): StandardRoute = {
    val json = objectMapper.writeValueAsBytes(result)
    complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity.apply(MediaTypes.`application/json`, json)))
  }

  protected def restFutureComplete(result: Future[Any]) = {
    handleFutureError(onComplete(result)) {
      res => {
        restComplete(res)
      }
    }
  }

  def restRoute: Route
}
