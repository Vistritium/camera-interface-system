package camerainterfacesystem.web.controllers.twirl

import akka.http.scaladsl.model.headers.CacheDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.web.AppController
import concurrent.duration._

class ResourcesController extends AppController {
  override def route: Route =
    CacheDirectives.`max-age`((1 hour).toSeconds) {
      pathPrefix("web") {
        getFromResourceDirectory("web", getClass.getClassLoader)
      } ~ get {
        pathPrefix("favicon.ico") {
          getFromResource("web/favicon.ico")
        }
      }
    }
}
