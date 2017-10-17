package camerainterfacesystem

import akka.actor.{ActorRef, ActorSystem, Props}
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.DB
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.services.ImageDataService
import camerainterfacesystem.web.WebServer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

object Main extends LazyLogging {

  val system = ActorSystem()
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  Azure.init()

  val web: ActorRef = system.actorOf(Props[WebServer], "web")
  val imageDataService: ActorRef = system.actorOf(Props[ImageDataService], "imagedataservice")

  def main(args: Array[String]): Unit = {
    logger.info(s"flyway migration: ${DB.migration}")

/*    val presets = List("gorka", "stol", "beczka", "ogrod wisnia", "altana", "doniczki", "las", "ogrod swierki", "hustawka")
    (0 to 1000).par.foreach(i => {
      Future {
        for {
          preset <- PresetsRepository.findPresetByNameOrCreateNew(Random.shuffle(presets).head)
          image <- ImagesRepository.addImage(Image(0, UUID.randomUUID().toString, "filename", Instant.now(), preset.id, 1))
        } yield (preset, image)
      }.flatten onComplete {
        case Failure(exception) => {
         logger.error(s"Error ${i}", exception)
          System.exit(0)
        }
        case Success(value) => {
          logger.info(s"Done ${i}")
        }
      }
    })*/

  }


  def tmp(images: Seq[(Image, Preset)]): Unit ={

    val json = Config.objectMapper.writeValueAsString(images.map(_._1))

  }

}
