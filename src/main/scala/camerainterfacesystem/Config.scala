package camerainterfacesystem

import java.time.ZoneId

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaModule}
import com.typesafe.config.{Config, ConfigFactory}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object Config {

  val config: Config = ConfigFactory.load()

  def apply(): Config = config

  val objectMapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }

  val userZone: ZoneId = ZoneId.of(config.getString("userTimezone"))

  val dryMode: Boolean = config.getBoolean("dryMode")
}
