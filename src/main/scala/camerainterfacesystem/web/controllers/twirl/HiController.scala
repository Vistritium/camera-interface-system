package camerainterfacesystem.web.controllers.twirl

import java.time.Instant

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.Main
import camerainterfacesystem.db.repos.PresetsRepository
import camerainterfacesystem.db.util.PresetId
import camerainterfacesystem.services.{CacheData, ImagesService}
import camerainterfacesystem.utils.FunctionalUtils
import camerainterfacesystem.web.{AppController, Controller}
import html.{index, newestPresetList}

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
    path("preset" / IntNumber) {
      (presetId) => {
        parameters("hour".as[Int].?, "min".as[Long].?, "max".as[Long].?) {
          (hourParam, minLong, maxLong) => {
            val min = minLong.map(Instant.ofEpochMilli)
            val max = maxLong.map(Instant.ofEpochMilli)

            val future = for {
              preset <- PresetsRepository.getPresetById(PresetId(presetId))
              presetEither = Either.cond(preset.isDefined, preset.get, "Preset couldn't be found")
              presetHours <- FunctionalUtils.reverseEitherFuture(presetEither.map(preset => PresetsRepository.getPresetsHours(preset.id)))
              hour = presetHours.map(presetHours => hourParam.getOrElse(presetHours.head.hour))
              images <- FunctionalUtils.reverseEitherFuture(hour.map(hour => {
                ImagesService.getNewestForPresetHour(presetId, hour, min, max).map(_._2)
              }))
              sortedImages = images.map(images => images.sortWith((l, r) => l.phototaken.isAfter(r.phototaken)))
            } yield (presetHours, hour, sortedImages, preset)

            val normalizedFuture = future.map(x => {
              x._1 match {
                case Left(text) => Left(text)
                case Right(value) => Right((value, x._2.right.get, x._3.right.get, x._4.get))
              }
            })

            handleFutureError(onComplete(normalizedFuture)) {
              case Left(text) => complete(HttpResponse(status = StatusCodes.Conflict, entity = text))
              case Right(res) => {
                htmlToResponseMarshalable {
                  res._3.foreach(image => Main.imageDataService ! CacheData(image.fullpath))
                  newestPresetList(hourParam.map(_ => res._3), res._4, res._2, res._1)
                }
              }
            }
          }
        }
      }
    }
  }
}




