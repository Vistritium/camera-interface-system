package camerainterfacesystem.web.controllers.rest.forms

import camerainterfacesystem.db.Tables.{Image, Preset}

case class ImagesWithPresetForHour(preset: Preset, images: Seq[Image])
