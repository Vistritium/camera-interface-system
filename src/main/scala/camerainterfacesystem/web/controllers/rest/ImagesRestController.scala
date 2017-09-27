package camerainterfacesystem.web.controllers.rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.db.repos.PresetsRepository
import camerainterfacesystem.web.controllers.rest.forms.PresetWithCount

class ImagesRestController extends AppRestController {

  override def restRoute: Route = path("presets") {
    handleFutureError(onComplete(PresetsRepository.getAllPresets())) {
      res =>
        val flatten = res
          .map(elem => PresetWithCount(elem._1, elem._2))
          .map(elem => elem
            .copy(preset = elem.preset
              .copy(displayname = Option(elem.preset.displayname.getOrElse(elem.preset.name)))))
        restComplete(flatten)
    }
  }

}
