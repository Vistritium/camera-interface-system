package camerainterfacesystem.services

import akka.pattern.ask
import camerainterfacesystem.Main
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.repos.ImagesRepository

import scala.concurrent.{ExecutionContext, Future}

object ImagesService extends AppService {

  def getNewestSnaps()(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset, Array[Byte])]] = {
    for {
      images <- ImagesRepository.getNewestImagesForAllPresets()
      withBytes <- Future.sequence(images.map(image => (Main.imageDataService ? GetData(image._1.fullpath))
        .mapTo[GetDataResult].map(res => (image._1, image._2, res.bytes))))
    } yield withBytes
  }
}