package camerainterfacesystem.web.controllers.rest

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.utils.PresetModelUtils
import camerainterfacesystem.web.controllers.Unmarshallers
import camerainterfacesystem.web.controllers.rest.forms.{ImagesWithPresetForHour, MetaImage, PresetWithCountAndHour}

import scala.concurrent.Future

class ImagesRestController extends AppRestController {

  override def restRoute: Route = path("presets") {
    handleFutureError(onComplete(PresetsRepository.getAllPresetsGroupedByHour())) {
      res =>
        val flatten = res
          .map(elem => PresetWithCountAndHour(elem._1, elem._2, PresetModelUtils.hourGTMToCurrent(elem._3)))
          .map(elem => elem
            .copy(preset = elem.preset
              .copy(displayname = Option(elem.preset.displayname.getOrElse(elem.preset.name)))))
        restComplete(flatten)
    }
  } ~ path("preset" / IntNumber / IntNumber) { (preset, hour) =>
    handleFutureError(onComplete(ImagesRepository.getImagesForPresetAndHour(PresetId(preset),
      PresetModelUtils.hourCurrentToGTM(Hour(hour))))) {
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

        restFutureComplete(ImagesRepository.deleteAllBetween(min, max, dryrun))
      }
    }
    } ~ path("images") {
      parameters(
        'min.as(Unmarshallers.instantUnmarshaller),
        'max.as(Unmarshallers.instantUnmarshaller) ? Instant.now(),
        'granulation.as[Int] ? 1, 'presets.as(Unmarshallers.seqIntUnmarshaller),
        'hours.as(Unmarshallers.seqIntUnmarshaller),
        'count.as[Boolean] ? false) { (min, max, granulation, presets, hours, count) =>
        val result: Future[Any] = if (!count) {
          ImagesRepository.findImages(presets.toSet, hours.toSet, min, max, granulation)
            .map(_
              .sortWith((l, r) => l._1.phototaken.isBefore(r._1.phototaken))
              .map(x => new MetaImage(x._1)))
        } else {
          ImagesRepository.findImagesCount(presets.toSet, hours.toSet, min, max, granulation)
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
            ImagesRepository.countImagesBetweenDates(min, max).map {
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
        handleFutureError(onComplete(ImagesRepository.getClosestImagesToDates(dates.toList, preset))) {
          a => restComplete(a)
        }
      }
    } ~ path("preview") {
      handleFutureError(onComplete(ImagesService.getPreview()))(restComplete)
    } ~ path("hours") {
      handleFutureError(onComplete(ImagesRepository.getAvailableHours()))(restComplete)
    } ~ path("bounds") {
      val bounds = for {
        minDate <- ImagesRepository.getEarliestDate()
        maxDate <- ImagesRepository.getLatestDate()
      } yield new {
        val min = minDate
        val max = maxDate
      }
      handleFutureError(onComplete(bounds))(restComplete)
    }
  }


}




