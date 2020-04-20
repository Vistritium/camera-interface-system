package camerainterfacesystem.db

import java.util.concurrent.Executors

import camerainterfacesystem.configs.DBConfig
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import slick.jdbc
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

@Singleton
class DB @Inject()(
  dBConfig: DBConfig
) extends LazyLogging {

  private val DatabaseFileName = "db.sqlite"
  private val databaseFile = dBConfig.dbPath.resolve(DatabaseFileName)
  private val dataSourceStr = s"jdbc:sqlite:${databaseFile.toString}"

  val migration: Int = {
    val flyway = new Flyway()
    flyway.setDataSource(dataSourceStr, null, null)
    flyway.migrate()
  }

  val sqlEc = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  //val slick: SQLiteProfile.backend.DatabaseDef = Database.forURL(dataSourceStr)
  val slick: SQLiteProfile.backend.DatabaseDef = Database.forURL(dataSourceStr, null, null, null, null, new AsyncExecutor {
    override def executionContext: ExecutionContext = sqlEc

    override def close(): Unit = ()
  }, keepAliveConnection = true)

  def apply(): jdbc.SQLiteProfile.backend.DatabaseDef = slick

}
