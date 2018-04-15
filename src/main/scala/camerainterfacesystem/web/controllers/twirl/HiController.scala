package camerainterfacesystem.web.controllers.twirl

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.Config
import camerainterfacesystem.db.repos.ImagesRepository
import camerainterfacesystem.services.ImagesService
import camerainterfacesystem.web.controllers.rest.GoogleDriveController
import camerainterfacesystem.web.{AppController, Controller}
import com.typesafe.scalalogging.LazyLogging
import html.index

@Controller
class HiController extends AppController with LazyLogging {

  private val coolHour: Int = Config.config.getInt("defaultHour")

  private val instantIsoDateFormatter = DateTimeFormatter.ISO_INSTANT

  override def route: Route = pathSingleSlash {
    get {

      GoogleDriveController.extractAuthData { googleAuthData =>
        val res = for {
          availableHours <- ImagesRepository.getAvailableHours()
          closestHour = availableHours.minBy(v => math.abs(v - coolHour))
          newestSnaps <- ImagesService.getNewestSnaps(Some(closestHour))
          minDate <- ImagesRepository.getEarliestDate()
          maxDate <- ImagesRepository.getLatestDate()
        } yield (availableHours, newestSnaps, minDate, maxDate)

        handleFutureError(onComplete(res)) {
          res => {
            val images = res._2.map(_._1)
            val hours = res._1
            htmlToResponseMarshalable(index(
              images,
              hours,
              instantIsoDateFormatter.format(res._3.getOrElse(Instant.now().minus(365, ChronoUnit.DAYS))),
              instantIsoDateFormatter.format(res._4.getOrElse(Instant.now())),
              googleAuthData
            ))
          }
        }
      }
    }
  }
}




