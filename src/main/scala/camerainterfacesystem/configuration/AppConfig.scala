package camerainterfacesystem.configuration

import java.time.ZoneId

case class AppConfig(
  userZone: ZoneId,
  dryMode: Boolean,
)
