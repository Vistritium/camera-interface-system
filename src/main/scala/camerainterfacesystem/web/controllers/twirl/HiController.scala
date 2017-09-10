package camerainterfacesystem.web.controllers.twirl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.web.{AppController, Controller}
import html.index

@Controller
class HiController extends AppController {

  override def route: Route = pathSingleSlash {
    get {
      handleFutureError(onComplete(ImagesService.getNewestSnaps())){
        newest => {
          htmlToResponseMarshalable(index(newest.map(_._1)))
        }
      }
    }
  }

}
