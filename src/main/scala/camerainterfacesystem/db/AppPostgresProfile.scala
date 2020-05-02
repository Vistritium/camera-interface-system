package camerainterfacesystem.db

import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait AppPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support {
  def pgjson = "jsonb"

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
    with DateTimeImplicits
}

object AppPostgresProfile extends AppPostgresProfile
