package camerainterfacesystem.web.controllers.twirl

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.web.{AppController, Controller}
import html.{imageSeries, index}

@Controller
class HiController extends AppController {

  override def route: Route = pathSingleSlash {
    get {
      handleFutureError(onComplete(ImagesService.getNewestSnaps())) {
        newest => {
          htmlToResponseMarshalable(index(newest.map(_._1)))
        }
      }
    }
  } ~ get {
    path("preset" / IntNumber / IntNumber) {
      (segmentId, limit) => {
        handleFutureError(onComplete(ImagesService.getNewestForPreset(segmentId, limit))) {
          newestForPreset => {
            val images = newestForPreset.map(_._1).sortWith((l, r) => l.phototaken.isAfter(r.phototaken))
            htmlToResponseMarshalable(imageSeries(images.map(x => x.fullpath -> None)))
          }
        }
      }
    }
  }

}
