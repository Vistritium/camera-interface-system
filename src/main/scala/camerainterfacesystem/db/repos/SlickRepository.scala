package camerainterfacesystem.db.repos

import java.time.Instant

import camerainterfacesystem.db.{DB, Tables}
import com.typesafe.scalalogging.LazyLogging
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcType, SQLiteProfile}

trait SlickRepository extends LazyLogging {

  protected val db: DB

  protected implicit val slick: SQLiteProfile.backend.DatabaseDef = db.apply()
  protected implicit val instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] = Tables.instantColumnType
}
