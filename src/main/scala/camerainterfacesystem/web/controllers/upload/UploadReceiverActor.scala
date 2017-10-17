package camerainterfacesystem.web.controllers.upload

import java.io.ByteArrayInputStream
import java.time.{OffsetDateTime, ZonedDateTime}
import java.util
import java.util.{Objects, UUID}

import camerainterfacesystem.azure.Azure
import camerainterfacesystem.db.Tables.Image
import camerainterfacesystem.db.repos.{ImagesRepository, PresetsRepository}
import camerainterfacesystem.utils.ImageUtils
import camerainterfacesystem.utils.ImageUtils.ImagePathProperties
import camerainterfacesystem.{AppActor, Config}
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class UploadReceiverActor(val onComplete: Promise[Unit]) extends AppActor {

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
          val props: ImagePathProperties = ImageUtils.extractPropertiesFromPath(filename) match {
            case Failure(exception) => {
              if (!onComplete.isCompleted) {
                onComplete.failure(exception)
              }
              throw new IllegalStateException(exception)
            }
            case Success(value) => value
          }
          val zonedDateTime: ZonedDateTime = ImageUtils.extractDateFromJpeg(bytes) match {
            case Failure(exception) => {
              onComplete.failure(exception)
              throw new IllegalStateException(exception)
            }
            case Success(value) => value
          }

          val future = if (!Config.dryMode) {
            for {
              preset <- PresetsRepository.findPresetByNameOrCreateNew(props.presetName)
              image <- ImagesRepository.addImage(Image(0, props.fullpath, props.filename, zonedDateTime.toInstant,
                preset.id, props.hourTaken))
            } yield Azure.upload(image.fullpath, new ByteArrayInputStream(bytes), bytes.length)
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