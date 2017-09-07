package camerainterfacesystem.web

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import camerainterfacesystem.AppActor
import org.reflections.Reflections
import org.reflections.scanners.{SubTypesScanner, TypeAnnotationsScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}

import scala.collection.JavaConverters._

class WebServer extends AppActor {

  private implicit val system = context.system
  private implicit val materializer = ActorMaterializer()(system)

  private val controllers: List[AppController] = {
    val reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage("camerainterfacesystem.web.controllers"))
      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner))
    val set = reflections.getTypesAnnotatedWith(classOf[Controller]).asScala.filterNot(_ == classOf[AppController])
    logger.info(s"Found following controllers: ${set.mkString("\n", "\n", "")}")
    set.map(_.newInstance().asInstanceOf[AppController]).toList
  }

  require(controllers.nonEmpty)
  private val route = controllers.map(_.route).reduce(_ ~ _)

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  override def receive = {
    case _ =>
  }

}


