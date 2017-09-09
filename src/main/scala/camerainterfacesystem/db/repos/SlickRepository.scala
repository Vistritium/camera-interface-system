package camerainterfacesystem.db.repos

import camerainterfacesystem.db.{DB, Tables}
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.SQLiteProfile

class SlickRepository extends LazyLogging {

  protected implicit val slick: SQLiteProfile.backend.DatabaseDef = DB()
  protected implicit val instantColumnType = Tables.instantColumnType

}
