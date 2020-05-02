package camerainterfacesystem.configs

import java.nio.file.Path

case class DBConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  database: String,
  schema: String
)
