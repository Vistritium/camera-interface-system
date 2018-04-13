package camerainterfacesystem

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZoneId
import java.util.Objects

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{Config, ConfigFactory}
import okhttp3.OkHttpClient

object Config {

  val config: Config = {
    val envConfigOpt = {
      var envConfig = System.getenv("ENV_CONFIG")
      if (Objects.isNull(envConfig)) {
        envConfig = System.getProperty("ENV_CONFIG")
      }
      if (Objects.nonNull(envConfig)) {
        Some(new File(envConfig))
      } else {
        val path = Paths.get("env.conf")
        if (Files.exists(path)) {
          Some(path.toFile)
        } else {
          None
        }
      }
    }

    (envConfigOpt match {
      case Some(envConfig) => ConfigFactory.parseFile(envConfig)
      case None => ConfigFactory.empty()
    }) withFallback ConfigFactory.load()
  }

  def apply(): Config = config

  val objectMapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }

  val httpClient: OkHttpClient = {
    new OkHttpClient()
  }

  val userZone: ZoneId = ZoneId.of(config.getString("userTimezone"))

  val dryMode: Boolean = config.getBoolean("dryMode")
}
