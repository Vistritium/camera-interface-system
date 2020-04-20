package camerainterfacesystem.web.controllers.rest

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.utils.PresetModelUtils
import camerainterfacesystem.web.controllers.Unmarshallers
import camerainterfacesystem.web.controllers.rest.forms.{ImagesWithPresetForHour, MetaImage}
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImagesRestController @Inject()(
  protected val objectMapper: ObjectMapper,
  override protected val system: ActorSystem,
  protected implicit val executionContext: ExecutionContext,
  presetsRepository: PresetsRepository,
  presetModelUtils: PresetModelUtils,
  imagesRepository: ImagesRepository,
  imagesService: ImagesService,
) extends AppRestController with LazyLogging {

  override def restRoute: Route = path("presets") {
    handleFutureError(onComplete(presetsRepository.getAllPresets()))(restComplete)
  } ~ path("preset" / IntNumber / IntNumber) { (preset, hour) =>
    handleFutureError(onComplete(imagesRepository.getImagesForPresetAndHour(PresetId(preset),
      presetModelUtils.hourCurrentToGTM(Hour(hour))))) {
      res =>
        restComplete {
          ImagesWithPresetForHour(res._1.normalizeName, res._2)
        }
    }
  } ~ get {
    path("images" / "min" / LongNumber / "max" / LongNumber) { (minEpoch, maxEpoch) => {
      parameter("dryrun" ? false) { dryrun =>
        val min = Instant.ofEpochMilli(minEpoch)
        val max = Instant.ofEpochMilli(maxEpoch)

        restFutureComplete(imagesRepository.deleteAllBetween(min, max, dryrun))
      }
    }
    } ~ path("images") {
      parameters(
        Symbol("min").as(Unmarshallers.instantUnmarshaller),
        Symbol("max").as(Unmarshallers.instantUnmarshaller) ? Instant.now(),
        Symbol("granulation").as[Int] ? 1, Symbol("presets").as(Unmarshallers.seqIntUnmarshaller),
        Symbol("hours").as(Unmarshallers.seqIntUnmarshaller),
        Symbol("count").as[Boolean] ? false) { (min, max, granulation, presets, hours, count) =>
        val result: Future[Any] = if (!count) {
          imagesRepository.findImages(presets.toSet, hours.toSet, min, max, granulation)
            .map(_
              .sortWith((l, r) => l._1.phototaken.isBefore(r._1.phototaken))
              .map(x => new MetaImage(x._1)))
        } else {
          imagesRepository.findImagesCount(presets.toSet, hours.toSet, min, max, granulation)
        }

        restFutureComplete(result)
      }
    } ~ pathPrefix("images" / "countFrom") {
      path(Segment / LongNumber) { (timeTypeString, count) =>
        val max = Instant.now().plus(1, ChronoUnit.DAYS)
        val min = {
          val timeType = ChronoUnit.valueOf(timeTypeString.toUpperCase)
          Instant.now().minus(count, timeType)
        }
        parameter('msgOnEmpty.as[Boolean] ? false) { msgOnEmpty =>
          restFutureComplete(
            imagesRepository.countImagesBetweenDates(min, max).map {
              case 0 if msgOnEmpty => "EMPTY"
              case other => other
            }
          )
        }
      }
    } ~ path("imagesClosestToDate" / "preset" / IntNumber) { preset =>
      parameter(
        'dates.as(Unmarshallers.commaSeparatedEpochDates)
      ) { dates =>
        handleFutureError(onComplete(imagesRepository.getClosestImagesToDates(dates.toList, preset))) {
          a => restComplete(a)
        }
      }
    } ~ path("preview") {
      handleFutureError(onComplete(imagesService.getPreview()))(restComplete)
    } ~ path("hours") {
      handleFutureError(onComplete(imagesRepository.getAvailableHours()))(restComplete)
    } ~ path("bounds") {
      val bounds = for {
        minDate <- imagesRepository.getEarliestDate()
        maxDate <- imagesRepository.getLatestDate()
      } yield new {
        val min = minDate
        val max = maxDate
      }
      handleFutureError(onComplete(bounds))(restComplete)
    }
  }
}




