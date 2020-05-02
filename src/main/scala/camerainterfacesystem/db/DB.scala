package camerainterfacesystem.db

import java.util.concurrent.Executors

import camerainterfacesystem.configs.DBConfig
import camerainterfacesystem.db.AppPostgresProfile.api._
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

import scala.concurrent.ExecutionContext

@Singleton
class DB @Inject()(
  dBConfig: DBConfig
) extends LazyLogging {

  private val dataSourceStr = s"jdbc:postgresql://${dBConfig.host}:${dBConfig.port}/${dBConfig.database}?currentSchema=${dBConfig.schema}"

  val migration: Int = {
    val flyway = new Flyway(
      new FluentConfiguration()
        .dataSource(dataSourceStr, dBConfig.user, dBConfig.password)
    )
    flyway.migrate()
  }

  val sqlEc = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())


  val slick: AppPostgresProfile.backend.DatabaseDef = Database.forURL(dataSourceStr, dBConfig.user, dBConfig.password, null, null, keepAliveConnection = true)

  def apply(): AppPostgresProfile.backend.DatabaseDef = slick

}
