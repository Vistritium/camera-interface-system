name := "camera-interface-system"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += "Adobe" at "https://repo.adobe.com/nexus/content/repositories/public/"
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val JacksonVersion = "2.10.1"
val AkkaVersion = "2.6.4"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.apache.httpcomponents" % "httpmime" % "4.5.5",
  "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "commons-io" % "commons-io" % "2.6",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "com.github.hipjim" %% "scala-retry" % "0.4.0",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1",
  "org.reflections" % "reflections" % "0.9.11",
  "org.synchronoss.cloud" % "nio-multipart-parser" % "1.1.0",
  "org.postgresql" % "postgresql" % "42.2.12",
  "org.flywaydb" % "flyway-core" % "6.4.1",
  "com.microsoft.azure" % "azure-storage" % "5.5.0",
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
  "com.github.tminglei" %% "slick-pg" % "0.19.0",
  "org.apache.commons" % "commons-imaging" % "1.0-R1534292",
  "com.github.ben-manes.caffeine" % "caffeine" % "2.5.5",
  "com.github.blemale" %% "scaffeine" % "4.0.0",
  "com.squareup.okhttp3" % "okhttp" % "3.10.0",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.2.3",
  "org.mapdb" % "mapdb" % "3.0.8",
  "net.coobird" % "thumbnailator" % "0.4.11",
  "com.iheart" %% "ficus" % "1.4.7"
)

javaOptions in Universal ++= Seq(
  "-J-Xmx512m",
  "-J-Xms512m",
  "-Ddatabase=/data",
  "-Dconfig.override_with_env_vars=true"
)

mainClass in Compile := Some("camerainterfacesystem.Main")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerRepository := Some("nowicki.azurecr.io")
dockerUsername := Some("nowicki")
dockerExposedPorts := Seq(8080)
dockerExposedVolumes := Seq("/data")
dockerUpdateLatest := true

Compile / unmanagedResourceDirectories += baseDirectory.value / "frontend" / "build"