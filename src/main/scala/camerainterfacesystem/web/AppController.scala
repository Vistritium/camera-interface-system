package camerainterfacesystem.web

import java.time.format.DateTimeFormatter

import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

@Controller
abstract class AppController extends LazyLogging with ControllerUtils {

  val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def route: Route

}
