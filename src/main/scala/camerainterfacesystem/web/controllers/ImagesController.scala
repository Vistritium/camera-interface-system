package camerainterfacesystem.web.controllers

import java.net.{URLDecoder, URLEncoder}

import akka.actor.ActorRef
import akka.http.scaladsl.model.MediaType.Compressible
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import camerainterfacesystem.Main
import camerainterfacesystem.web.{AppController, Controller}
import akka.pattern.ask
import camerainterfacesystem.services.{GetData, GetDataResult, ImagesService}

import scala.util.{Failure, Success}

@Controller
class ImagesController extends AppController {

  private val imageDataService: ActorRef = Main.imageDataService

  override def route = pathPrefix("images") {
    path("newest") {
      get {
        handleFutureError(onComplete(ImagesService.getNewestSnaps())) {
          images => {
            complete(???)
          }
        }
      }
    } ~ get {
      path("download" / RemainingPath) { path =>
        val remaining = URLDecoder.decode(path.toString(), "utf-8")
        handleFutureError(onComplete((imageDataService ? GetData(remaining)).mapTo[GetDataResult])) {
          data =>
            complete(HttpResponse(entity = HttpEntity(ContentType(MediaType.image("jpeg", Compressible, ".jpg")), data.bytes)))
        }
      }
    } ~ delete {
      path(IntNumber) {
        id =>
          complete(id.toString)
      }
    }
  }
}
