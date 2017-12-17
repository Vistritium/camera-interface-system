package camerainterfacesystem.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import camerainterfacesystem.{AppActor, Config}
import camerainterfacesystem.web.controllers.rest.AppRestController
import org.reflections.Reflections
import org.reflections.scanners.{SubTypesScanner, TypeAnnotationsScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import scala.collection.JavaConverters._

class WebServer extends AppActor {

  private implicit val system: ActorSystem = context.system
  private implicit val materializer: ActorMaterializer = {
    val mat = ActorMaterializer()(context)
    WebServer.webMaterializer = mat
    mat
  }

  private val controllers: List[AppController] = {
    val reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage("camerainterfacesystem.web.controllers"))
      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner))
    val set = reflections.getTypesAnnotatedWith(classOf[Controller]).asScala
      .filterNot(_ == classOf[AppController])
      .filterNot(_ == classOf[AppRestController])
    logger.info(s"Found following controllers: ${set.mkString("\n", "\n", "")}")
    set.map(_.newInstance().asInstanceOf[AppController]).toList
  }

  require(controllers.nonEmpty)
  private val route = controllers.map(_.route).reduce(_ ~ _)

  private val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", Config.config.getInt("port"))

  override def receive = {
    case _ =>
  }

}

object WebServer {
  var webMaterializer: ActorMaterializer = _
}


