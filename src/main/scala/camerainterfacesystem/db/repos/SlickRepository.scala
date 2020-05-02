package camerainterfacesystem.db.repos

import camerainterfacesystem.db.{AppPostgresProfile, DB}
import com.typesafe.scalalogging.LazyLogging

trait SlickRepository extends LazyLogging {

  protected val db: DB

  protected implicit val slick: AppPostgresProfile.backend.DatabaseDef = db.apply()
}
