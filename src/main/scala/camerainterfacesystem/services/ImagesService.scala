package camerainterfacesystem.services

import java.time.Instant

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.configuration.ConfigLoader
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.services.akkap.ImageDataService
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImagesService @Inject()(
  protected implicit val executionContext: ExecutionContext,
  imagesRepository: ImagesRepository,
  presetsRepository: PresetsRepository,
  @Named(AkkaRefNames.ImageDataService) imageDataService: ActorRef[ImageDataService.Command],
  override protected val system: ActorSystem,
  azure: Azure,
) extends AppService with LazyLogging {

  private val coolHour: Int = ConfigLoader.config.getInt("defaultHour")

  def getNewestSnaps(hour: Option[Int] = None)(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset, Array[Byte])]] = {
    for {
      images <- hour match {
        case Some(hour) => imagesRepository.getNewestImagesForAllPresets(hour)
        case None => imagesRepository.getNewestImagesForAllPresets()
      }
      imageIdToPreset = images.map(x => x._1.id -> x._2).toMap
      withBytes <- getBytes(images.map(_._1))
    } yield withBytes.map(x => (x._1, imageIdToPreset(x._1.id), x._2))
  }

  def getNewestForPresetWithBytes(presetId: Int, limit: Int)(implicit executionContext: ExecutionContext): Future[(Seq[(Image, Array[Byte])], Option[Preset])] = {
    for {
      preset <- presetsRepository.getPresetById(PresetId(presetId))
      images <- imagesRepository.getNewestImagesForPreset(presetId, limit)
      withBytes <- getBytes(images)
    } yield withBytes -> preset
  }

  def getNewestForPreset(presetId: Int, limit: Int)(implicit executionContext: ExecutionContext): Future[(Seq[(Image)], Option[Preset])] = {
    for {
      preset <- presetsRepository.getPresetById(PresetId(presetId))
      images <- imagesRepository.getNewestImagesForPreset(presetId, limit)
    } yield images -> preset
  }

  def getNewestForPresetHour(presetId: Int, hour: Int, min: Option[Instant], max: Option[Instant])(implicit executionContext: ExecutionContext): Future[(Preset, Seq[Image])] = {
    for {
      images <- imagesRepository.getImagesForPresetAndHour(PresetId(presetId), Hour(hour), min, max)
    } yield images
  }

  def deleteImage(idOrFullpath: Either[Int, String]): Future[Boolean] = {
    //noinspection UnitInMap
    for {
      maybeImage <- imagesRepository.getImage(idOrFullpath)
      deletedFromDB = maybeImage.map(image => {
        imagesRepository.deleteImage(Left(image.id))
        image
      })
      deletedFromCloud = deletedFromDB.map(image => azure.deleteBlob(image.fullpath))
    } yield deletedFromCloud.isDefined
  }

  def getSpecificDates(dates: List[Instant], preset: Int): Future[List[(Instant, Option[Image])]] = {
    imagesRepository.getClosestImagesToDates(dates, preset)
  }

  private def getBytes(images: Seq[(Image)]) = {
    Future.sequence(images.map(image => {
      imageDataService
        .ask[ImageDataService.GetDataResult](ref => ImageDataService.GetData(image.fullpath, ref))
        .map(res => (image, res.bytes))
    }))
  }

  def getPreview(): Future[Seq[Image]] = {
    for {
      availableHours <- imagesRepository.getAvailableHours()
      closestHour = availableHours.minBy(v => math.abs(v - coolHour))
      newestSnaps <- getNewestSnaps(Some(closestHour))
      images = newestSnaps.map(_._1)
    } yield images
  }
}