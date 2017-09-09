package camerainterfacesystem.db

import java.nio.file.{Files, Paths}

import slick.jdbc.SQLiteProfile.api._
import camerainterfacesystem.Config
import org.flywaydb.core.Flyway
import slick.jdbc
import slick.jdbc.SQLiteProfile

object DB {

  private val DatabaseFileName = "db.sqlite"

  private val databaseFile = {
    val str = Config().getString("database")
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

  val slick: SQLiteProfile.backend.DatabaseDef = Database.forURL(dataSourceStr)
  def apply(): jdbc.SQLiteProfile.backend.DatabaseDef = slick

}
