package camerainterfacesystem.utils

import java.time.{ZoneOffset, ZonedDateTime}

import camerainterfacesystem.configuration.{AppConfig, ConfigLoader}
import camerainterfacesystem.db.util.Hour
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

@Singleton
class PresetModelUtils @Inject()(
  appConfig: AppConfig
) extends LazyLogging {

  def hourGTMToCurrent(hour: Hour): Hour = {
    Hour(
      ZonedDateTime.now(ZoneOffset.UTC)
        .withHour(hour.hour)
        .withZoneSameInstant(appConfig.userZone)
        .getHour
    )
  }

  def hourCurrentToGTM(hour: Hour): Hour = {
    Hour(
      ZonedDateTime.now(appConfig.userZone)
        .withHour(hour.hour)
        .withZoneSameInstant(ZoneOffset.UTC)
        .getHour
    )
  }

}
