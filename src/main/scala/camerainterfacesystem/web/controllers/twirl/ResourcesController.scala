package camerainterfacesystem.web.controllers.twirl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.web.AppController

class  ResourcesController extends AppController {
  override def route: Route = pathPrefix("web") {
    getFromResourceDirectory("web", getClass.getClassLoader)
  }
}
