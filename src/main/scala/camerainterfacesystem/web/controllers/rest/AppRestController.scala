package camerainterfacesystem.web.controllers.rest

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import camerainterfacesystem.Config
import camerainterfacesystem.web.AppController

abstract class AppRestController extends AppController {

  private val objectMapper = Config.objectMapper

  override def route: Route = pathPrefix("api") {
    restRoute
  }

  protected def restComplete(result: AnyRef): StandardRoute = {
    val json = objectMapper.writeValueAsBytes(result)
    complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity.apply(MediaTypes.`application/json`, json)))
  }

  def restRoute: Route
}
