package camerainterfacesystem.web.controllers.twirl

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.Config
import camerainterfacesystem.db.repos.ImagesRepository
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.web.{AppController, Controller}
import com.typesafe.scalalogging.LazyLogging
import html.index

@Controller
class HiController extends AppController with LazyLogging {

  private val coolHour: Int = Config.config.getInt("defaultHour")

  private val instantIsoDateFormatter = DateTimeFormatter.ISO_INSTANT

  override def route: Route = pathSingleSlash {
    get {
      val res = for {
        preview <- ImagesService.getPreview()
        hours <- ImagesRepository.getAvailableHours()
        minDate <- ImagesRepository.getEarliestDate()
        maxDate <- ImagesRepository.getLatestDate()
      } yield (preview, hours, minDate, maxDate)

      handleFutureError(onComplete(res)) {
        res => {
          val hours = res._1
          htmlToResponseMarshalable(index(
            res._1,
            res._2,
            instantIsoDateFormatter.format(res._3.getOrElse(Instant.now().minus(365, ChronoUnit.DAYS))),
            instantIsoDateFormatter.format(res._4.getOrElse(Instant.now()))
          ))
        }
      }
    }
  }
}


object DaMain {

  def main(args: Array[String]): Unit = {
    List("a", "b", "c")
  }

}

