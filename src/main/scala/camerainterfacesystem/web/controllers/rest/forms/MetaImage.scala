package camerainterfacesystem.web.controllers.rest.forms

import java.time.Instant

import camerainterfacesystem.db.Tables.Image

case class MetaImage(fullpath: String, phototaken: Instant, presetId: Int) {

  def this(image: Image) = this(image.fullpath, image.phototaken, image.presetid)

}
