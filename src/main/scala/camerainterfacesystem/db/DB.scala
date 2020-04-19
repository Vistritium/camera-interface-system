package camerainterfacesystem.db

import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import slick.jdbc
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

@Singleton
class DB @Inject()(
  config: Config
) extends LazyLogging {

  private val DatabaseFileName = "db.sqlite"

  private val databaseFile = {
    val str = config.getString("database")
    val path = Paths.get(str)
    require(Files.isDirectory(path), s"${path} must be directory")
    require(Files.isWritable(path), s"${path} must be writable")
    path.resolve(DatabaseFileName)
  }

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
