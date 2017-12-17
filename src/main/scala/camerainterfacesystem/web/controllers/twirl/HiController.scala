package camerainterfacesystem.web.controllers.twirl

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.{Config, Main}
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.PresetId
import camerainterfacesystem.services.{CacheData, ImagesService}
import camerainterfacesystem.utils.FunctionalUtils
import camerainterfacesystem.web.{AppController, Controller}
import com.typesafe.scalalogging.LazyLogging
import html.{index, newestPresetList}

@Controller
class HiController extends AppController with LazyLogging {

  private val coolHour: Int = Config.config.getInt("defaultHour")

  private val instantIsoDateFormatter = DateTimeFormatter.ISO_INSTANT

  override def route: Route = pathSingleSlash {
    get {

      val res = for {
        availableHours <- ImagesRepository.getAvailableHours()
        closestHour = availableHours.minBy(v => math.abs(v - coolHour))
        newestSnaps <- ImagesService.getNewestSnaps(Some(closestHour))
        minDate <- ImagesRepository.getEarliestDate()
        maxDate <- ImagesRepository.getLatestDate()
      } yield (availableHours, newestSnaps, minDate, maxDate)


      handleFutureError(onComplete(res)) {
        res => {
          val images = res._2.map(_._1)
          val hours = res._1
          htmlToResponseMarshalable(index(images, hours,
            instantIsoDateFormatter.format(res._3.getOrElse(Instant.now().minus(365, ChronoUnit.DAYS))),
            instantIsoDateFormatter.format(res._4.getOrElse(Instant.now()))
          ))
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




