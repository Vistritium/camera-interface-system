package camerainterfacesystem.web.controllers.rest.forms

import java.time.OffsetDateTime

import camerainterfacesystem.db.Tables.Image

case class MetaImage(fullpath: String, phototaken: OffsetDateTime, presetId: Int) {

  def this(image: Image) = this(image.fullpath, image.phototaken, image.presetid)

}
