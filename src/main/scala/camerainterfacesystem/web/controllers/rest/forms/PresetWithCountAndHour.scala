package camerainterfacesystem.web.controllers.rest.forms

import camerainterfacesystem.db.Tables.Preset
import camerainterfacesystem.db.util.{Count, Hour}
import com.fasterxml.jackson.annotation.JsonUnwrapped

case class PresetWithCountAndHour(@JsonUnwrapped preset: Preset, @JsonUnwrapped count: Count, @JsonUnwrapped hour: Hour) {}
