package camerainterfacesystem.services

import akka.pattern.ask
import camerainterfacesystem.Main
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.repos.ImagesRepository

import scala.concurrent.{ExecutionContext, Future}

object ImagesService extends AppService {

  def getNewestSnaps()(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset, Array[Byte])]] = {
    for {
      images <- ImagesRepository.getNewestImagesForAllPresets()
      withBytes <- getBytes(images)
    } yield withBytes
  }

  def getNewestForPreset(presetId: Int, limit: Int)(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset, Array[Byte])]] = {
    for {
      images <- ImagesRepository.getNewestImagesForPreset(presetId, limit)
      withBytes <- getBytes(images)
    } yield withBytes
  }

  def deleteImage(idOrFullpath: Either[Int, String]): Future[Boolean] = {
    //noinspection UnitInMap
    for {
      maybeImage <- ImagesRepository.getImage(idOrFullpath)
      deletedFromDB = maybeImage.map(image => {
        ImagesRepository.deleteImage(Left(image.id))
        image
      })
      deletedFromCloud = deletedFromDB.map(image => Azure.deleteBlob(image.fullpath))
    } yield deletedFromCloud.isDefined
  }

  private def getBytes(images: Seq[(Image, Preset)]) = {
    Future.sequence(images.map(image => (Main.imageDataService ? GetData(image._1.fullpath))
      .mapTo[GetDataResult].map(res => (image._1, image._2, res.bytes))))
  }
}