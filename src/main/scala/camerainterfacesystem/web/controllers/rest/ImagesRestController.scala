package camerainterfacesystem.web.controllers.rest

import java.time.{Instant, OffsetDateTime, ZoneOffset}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.utils.PresetModelUtils
import camerainterfacesystem.web.controllers.rest.forms.{ImagesWithPresetForHour, PresetWithCountAndHour}
import com.fasterxml.jackson.databind.util.ISO8601DateFormat

class ImagesRestController extends AppRestController {

  override def restRoute: Route = path("presets") {
    handleFutureError(onComplete(PresetsRepository.getAllPresets())) {
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
  } ~ path("images" / "min" / LongNumber / "max" / LongNumber) { (minEpoch, maxEpoch) => {
    parameter("dryrun" ? false) { dryrun =>
      val min = Instant.ofEpochMilli(minEpoch)
      val max = Instant.ofEpochMilli(maxEpoch)

      handleFutureError(onComplete(ImagesRepository.deleteAllBetween(min, max, dryrun))) {
        res =>
          restComplete(res)
      }
    }
  }
  }

}
