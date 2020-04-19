package camerainterfacesystem.configuration

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Objects

import com.typesafe.config.{ConfigFactory, Config}

object ConfigLoader {

  def config: Config = {
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


}
