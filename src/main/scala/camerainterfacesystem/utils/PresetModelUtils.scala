package camerainterfacesystem.utils

import java.time.{ZoneOffset, ZonedDateTime}

import camerainterfacesystem.Config
import camerainterfacesystem.db.util.Hour

object PresetModelUtils {

  def hourGTMToCurrent(hour: Hour): Hour = {
    Hour(
      ZonedDateTime.now(ZoneOffset.UTC)
        .withHour(hour.hour)
        .withZoneSameInstant(Config.userZone)
        .getHour
    )
  }

}
