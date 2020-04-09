name := "camera-interface-system"

version := "0.1"

scalaVersion := "2.12.10"

resolvers += "Adobe" at "https://repo.adobe.com/nexus/content/repositories/public/"
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",
  "org.apache.httpcomponents" % "httpmime" % "4.5.5",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.1",
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "commons-io" % "commons-io" % "2.5",
  "org.apache.commons" % "commons-lang3" % "3.7",
  "com.github.hipjim" %% "scala-retry" % "0.2.2",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "org.reflections" % "reflections" % "0.9.11",
  "org.synchronoss.cloud" % "nio-multipart-parser" % "1.1.0",
  "org.xerial" % "sqlite-jdbc" % "3.20.0" % "runtime",
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "com.microsoft.azure" % "azure-storage" % "5.5.0",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.2.1",
  "org.apache.commons" % "commons-imaging" % "1.0-R1534292",
  "com.github.ben-manes.caffeine" % "caffeine" % "2.5.5",
  "com.github.blemale" %% "scaffeine" % "2.2.0",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.squareup.okhttp3" % "okhttp" % "3.10.0"
)

javaOptions in Universal ++= Seq(
  "-J-Xmx512m",
  "-J-Xms512m",
  "-Ddatabase=/data",
  "-DENV_CONFIG=/data/env.config"
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