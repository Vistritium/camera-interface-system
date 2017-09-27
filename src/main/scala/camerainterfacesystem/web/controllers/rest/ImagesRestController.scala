package camerainterfacesystem.web.controllers.rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.db.repos.PresetsRepository
import camerainterfacesystem.utils.PresetModelUtils
import camerainterfacesystem.web.controllers.rest.forms.PresetWithCountAndHour

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
  }

}
