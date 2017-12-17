package camerainterfacesystem.services

import java.time.Instant

import akka.pattern.ask
import camerainterfacesystem.Main
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.{Hour, PresetId}

import scala.concurrent.{ExecutionContext, Future}

object ImagesService extends AppService {

  def getNewestSnaps(hour: Option[Int] = None)(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset, Array[Byte])]] = {
    for {
      images <- hour match {
        case Some(hour) => ImagesRepository.getNewestImagesForAllPresets(hour)
        case None => ImagesRepository.getNewestImagesForAllPresets()
      }
      imageIdToPreset = images.map(x => x._1.id -> x._2).toMap
      withBytes <- getBytes(images.map(_._1))
    } yield withBytes.map(x => (x._1, imageIdToPreset(x._1.id), x._2))
  }

  def getNewestForPresetWithBytes(presetId: Int, limit: Int)(implicit executionContext: ExecutionContext): Future[(Seq[(Image, Array[Byte])], Option[Preset])] = {
    for {
      preset <- PresetsRepository.getPresetById(PresetId(presetId))
      images <- ImagesRepository.getNewestImagesForPreset(presetId, limit)
      withBytes <- getBytes(images)
    } yield withBytes -> preset
  }

  def getNewestForPreset(presetId: Int, limit: Int)(implicit executionContext: ExecutionContext): Future[(Seq[(Image)], Option[Preset])] = {
    for {
      preset <- PresetsRepository.getPresetById(PresetId(presetId))
      images <- ImagesRepository.getNewestImagesForPreset(presetId, limit)
    } yield images -> preset
  }

  def getNewestForPresetHour(presetId: Int, hour: Int, min: Option[Instant], max: Option[Instant])(implicit executionContext: ExecutionContext): Future[(Preset, Seq[Image])] = {
    for {
      images <- ImagesRepository.getImagesForPresetAndHour(PresetId(presetId), Hour(hour), min, max)
    } yield images
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

  private def getBytes(images: Seq[(Image)]) = {
    Future.sequence(images.map(image => (Main.imageDataService ? GetData(image.fullpath))
      .mapTo[GetDataResult].map(res => (image, res.bytes))))
  }
}