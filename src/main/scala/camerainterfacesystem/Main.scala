package camerainterfacesystem

import camerainterfacesystem.configuration.{ConfigLoader, MainModule}
import camerainterfacesystem.db.DB
import com.google.inject.Guice
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(new MainModule(ConfigLoader.config))
    injector.getInstance(classOf[Starter]).start()

    //    val web: ActorRef = system.actorOf(Props[WebServer], "web")
    //    val imageDataService: ActorRef = system.actorOf(Props[ImageDataService], "imagedataservice")


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


}
