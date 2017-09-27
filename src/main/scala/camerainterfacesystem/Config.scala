package camerainterfacesystem

import java.time.ZoneId

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaModule}
import com.typesafe.config.{Config, ConfigFactory}

object Config {

  val config: Config = ConfigFactory.load()

  def apply(): Config = config

  val objectMapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }

  val userZone: ZoneId = ZoneId.of(config.getString("userTimezone"))
}
