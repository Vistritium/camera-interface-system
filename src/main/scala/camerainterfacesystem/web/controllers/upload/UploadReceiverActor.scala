package camerainterfacesystem.web.controllers.upload

import java.io.ByteArrayInputStream
import java.time.ZonedDateTime
import java.util
import java.util.{Objects, UUID}

import camerainterfacesystem.AppActor
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.configuration.AppConfig
import camerainterfacesystem.db.Tables.Image
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.utils.{ImagePathProperties, ImageUtils}
import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import org.apache.commons.lang3.StringUtils

import scala.concurrent.{Future, Promise}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class UploadReceiverActor @Inject()(
  @Assisted val onComplete: Promise[Unit],
  imageUtils: ImageUtils,
  appConfig: AppConfig,
  presetsRepository: PresetsRepository,
  imagesRepository: ImagesRepository,
  azure: Azure
) extends AppActor {

  private var allPartsFinished = false

  private var processingFiles: Set[String] = Set()

  override def receive: Receive = {

    case OnError(message, cause) => {
      onComplete.failure(new RuntimeException(message, cause))
      context.stop(self)
    }

    case OnPartFinished(bytes, headers) => {
      val system = context.system
      val self = this.self
      Option(headers.get("content-disposition")).flatMap(_.asScala.headOption) match {
        case None => {
          if (!onComplete.isCompleted) {
            onComplete.failure(new IllegalStateException(s"Couldn't read filename from headers: $headers"))
          }
          system.stop(self)
        }
        case Some(contentDispotionString) => {
          val filename = extractFilename(contentDispotionString)
          val processingId = UUID.randomUUID().toString.substring(0, 5) + " " + filename
          processingFiles = processingFiles + processingId
          val props: ImagePathProperties = imageUtils.extractPropertiesFromPath(filename) match {
            case Failure(exception) => {
              if (!onComplete.isCompleted) {
                onComplete.failure(exception)
              }
              throw new IllegalStateException(exception)
            }
            case Success(value) => value
          }
          val zonedDateTime: ZonedDateTime = imageUtils.extractDateFromJpeg(bytes) match {
            case Failure(exception) => {
              onComplete.failure(exception)
              throw new IllegalStateException(exception)
            }
            case Success(value) => value
          }

          val future = if (!appConfig.dryMode) {
            for {
              preset <- presetsRepository.findPresetByNameOrCreateNew(props.presetName)
              image <- imagesRepository.addImage(Image(0, props.fullpath, props.filename, zonedDateTime.toInstant,
                preset.id, props.hourTaken))
            } yield azure.upload(image.fullpath, new ByteArrayInputStream(bytes), bytes.length)
          } else {
            Future {"dry mode"}
          }

          future.onComplete {
            case Failure(exception) => {
              if (!onComplete.isCompleted) {
                onComplete.failure(exception)
              }

              system.stop(self)
            }
            case Success(_) => {
              processingFiles = processingFiles - processingId
              checkAndFinishIfCompleted()
            }
          }
        }
      }
    }

    case OnAllPartsFinished => {
      allPartsFinished = true
      checkAndFinishIfCompleted()
    }
  }

  def checkAndFinishIfCompleted(): Unit = {
    if (allPartsFinished && processingFiles.isEmpty) {
      onComplete.success(Success())
    }
  }

  def extractFilename(contentDisposition: String): String = {
    val res = StringUtils.substringBetween(contentDisposition, "filename=\"", "\"")
    require(Objects.nonNull(res), s"couldn't find filename in content disposition string: ${contentDisposition}")
    res
  }

  override def postStop(): Unit = {
    if (!onComplete.isCompleted) {
      onComplete.failure(new IllegalStateException("Actor stopped before finishing job"))
    }
    super.postStop()
  }
}

case class OnError(message: String, cause: Throwable)

case class OnPartFinished(bytes: Array[Byte], headersFromPart: util.Map[String, util.List[String]])

case object OnAllPartsFinished

case object OnNestedPartFinished

case class OnNestedPartStarted(headersFromParentPart: util.Map[String, util.List[String]])