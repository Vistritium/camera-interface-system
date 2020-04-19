package camerainterfacesystem.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import camerainterfacesystem.web.controllers.FrontendSpecialController
import camerainterfacesystem.web.controllers.rest.AppRestController
import camerainterfacesystem.CORSHandler
import camerainterfacesystem.configuration.ConfigLoader
import com.google.inject.{Inject, Injector, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.reflections.Reflections
import org.reflections.scanners.{SubTypesScanner, TypeAnnotationsScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Singleton
class WebServer @Inject()(
  private implicit val system: ActorSystem,
  injector: Injector
) extends LazyLogging {

  private val controllers: List[AppController] = {
    val reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage("camerainterfacesystem.web.controllers"))
      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner))
    val set = reflections.getTypesAnnotatedWith(classOf[Controller]).asScala
      .filterNot(_ == classOf[AppController])
      .filterNot(_ == classOf[AppRestController])
    logger.info(s"Found following controllers: ${set.mkString("\n", "\n", "")}")
    set.map(clazz => injector.getInstance(clazz).asInstanceOf[AppController]).toList
  }

  require(controllers.nonEmpty)
  private val route = CORSHandler.corsHandler {
    val standardControllers = controllers.map(_.route).reduce(_ ~ _)
    val frontendSpecialController = new FrontendSpecialController
    (standardControllers ~ frontendSpecialController.route)
  }

  def start(): Future[Http.ServerBinding] = {
    val port: Int = ConfigLoader.config.getInt("port")
    logger.info(s"Server started on $port")
    Http().bindAndHandle(route, "0.0.0.0", port)
  }


}



