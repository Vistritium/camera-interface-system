package camerainterfacesystem.web.controllers

import akka.http.scaladsl.model.headers.CacheDirectives._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._

class FrontendSpecialController {
  def route: Route =
    respondWithHeader(`Cache-Control`(`public`, `max-age`((1 hour).toSeconds))) {
      pathSingleSlash {
        getFromResource("frontend/index.html")
      } ~ getFromResourceDirectory("frontend")
    }
}
