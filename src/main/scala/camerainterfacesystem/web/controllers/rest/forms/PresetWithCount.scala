package camerainterfacesystem.web.controllers.rest.forms

import camerainterfacesystem.db.Tables.Preset
import camerainterfacesystem.db.util.Count
import com.fasterxml.jackson.annotation.JsonUnwrapped

case class PresetWithCount(@JsonUnwrapped preset: Preset, @JsonUnwrapped count: Count) {}
