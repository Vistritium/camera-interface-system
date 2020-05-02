package camerainterfacesystem.configuration

import java.nio.file.{Files, Paths}
import java.time.ZoneId

import akka.actor.ActorSystem
import camerainterfacesystem.configs.{DBConfig, LocalStorageConfig}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.ScalaModule
import okhttp3.OkHttpClient
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.reflections.Reflections
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.commons.lang3.StringUtils

import scala.concurrent.ExecutionContext

class MainModule(config: Config) extends ScalaModule with LazyLogging {

  override def configure(): Unit =
    new Reflections(getClass.getPackage.getName)
      .getTypesAnnotatedWith(classOf[Configuration])
      .forEach { c =>
        logger.debug(s"Installing $c")
        install(c.newInstance().asInstanceOf[AbstractModule])
      }

  @Provides
  @Singleton
  def provideConfig(): Config = config

  @Provides
  @Singleton
  def provideAppConfig(config: Config): AppConfig = AppConfig(
    ZoneId.of(config.getString("userTimezone")),
    config.getBoolean("dryMode")
  )

  @Provides
  @Singleton
  def objectMapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }

  @Provides
  @Singleton
  def httpClient(): OkHttpClient = {
    new OkHttpClient()
  }

  @Provides
  @Singleton
  def apacheHttpClient(): CloseableHttpClient = {
    HttpClients.createMinimal()
  }

  @Provides
  @Singleton
  def defaultContext(actorSystem: ActorSystem): ExecutionContext = actorSystem.dispatcher

  @Provides
  @Singleton
  def dbPath(): DBConfig = {
    val result = config.as[DBConfig]("db")
    if (StringUtils.isBlank(result.password)) throw new IllegalStateException("db password is empty")
    result
  }


  @Provides
  @Singleton
  def localStorageConfig(): LocalStorageConfig = {
    val path = Paths.get(config.getString("database"))
    require(Files.isDirectory(path), s"${path} must be directory")
    require(Files.isWritable(path), s"${path} must be writable")
    LocalStorageConfig(
      path
    )
  }

}
