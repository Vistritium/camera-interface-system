package camerainterfacesystem.web.controllers

import java.net.URLDecoder

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.MediaType.Compressible
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import camerainterfacesystem.services.akkap.ImageDataService
import camerainterfacesystem.services.{AkkaRefNames, ImagesService}
import camerainterfacesystem.web.AppController
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.ByteString
import camerainterfacesystem.services.cache.ThumbnailCache

import scala.concurrent.ExecutionContext

@Singleton
class ImagesController @Inject()(
  @Named(AkkaRefNames.ImageDataService) imageDataService: ActorRef[ImageDataService.Command],
  protected implicit val executionContext: ExecutionContext,
  imagesService: ImagesService,
  thumbnailCache: ThumbnailCache,
  override protected val system: ActorSystem
) extends AppController with LazyLogging {

  override def route = pathPrefix("images") {
    path("newest") {
      get {
        handleFutureError(onComplete(imagesService.getNewestSnaps())) {
          images => {
            complete(???)
          }
        }
      }
    } ~ get {
      path("download" / RemainingPath) { path =>

        parameter(Symbol("thumbnail").as[Boolean] ? false) { thumbnail =>
          val fullPath = URLDecoder.decode(path.toString(), "utf-8")

          val eventualResult =
            if (!thumbnail)
              imageDataService.ask[ImageDataService.GetDataResult](ref => ImageDataService.GetData(fullPath, ref)).map(r => ByteString(r.bytes))
            else
              thumbnailCache.getThumbnail(fullPath)
              handleFutureError(onComplete(eventualResult)) {
                data =>
                  complete(HttpResponse(entity = HttpEntity(ContentType(MediaType.image("jpeg", Compressible, ".jpg")), data)))
              }
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
