package camerainterfacesystem.db.repos

import java.time.Instant

import camerainterfacesystem.db.{DB, Tables}
import com.typesafe.scalalogging.LazyLogging
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcType, SQLiteProfile}

class SlickRepository extends LazyLogging {

  protected implicit val slick: SQLiteProfile.backend.DatabaseDef = DB()
  protected implicit val instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] = Tables.instantColumnType
}
