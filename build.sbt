name := "camera-interface-system"

version := "0.1"

scalaVersion := "2.12.3"

resolvers += "Adobe" at "https://repo.adobe.com/nexus/content/repositories/public/"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.3"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.0"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.4"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.10"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.6"
libraryDependencies += "com.github.hipjim" %% "scala-retry" % "0.2.2"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
libraryDependencies += "org.reflections" % "reflections" % "0.9.11"
libraryDependencies += "org.synchronoss.cloud" % "nio-multipart-parser" % "1.1.0"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.20.0" % "runtime"
libraryDependencies += "org.flywaydb" % "flyway-core" % "4.2.0"
libraryDependencies += "com.microsoft.azure" % "azure-storage" % "5.5.0"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.1"
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.2.1"
libraryDependencies += "org.apache.commons" % "commons-imaging" % "1.0-R1534292"

javaOptions in Universal ++= Seq(
  "-J-Xmx512m",
  "-J-Xms512m",
  "-Ddatabase=/data"
)

enablePlugins(SbtTwirl)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerRepository := Some("nowicki.azurecr.io")
dockerUsername := Some("nowicki")
dockerExposedPorts := Seq(8080)
dockerExposedVolumes := Seq("/data")
dockerUpdateLatest := true

