package camerainterfacesystem.web.controllers.upload

import java.io.ByteArrayInputStream
import java.util

import camerainterfacesystem.AppActor
import org.apache.commons.io.IOUtils
import org.synchronoss.cloud.nio.stream.storage.StreamStorage

import scala.collection.JavaConverters._
import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

class UploadReceiverActor(val onComplete: Promise[Try[Unit]]) extends AppActor {

  override def receive = {

    case OnError(message, cause) => {
      onComplete.success(Failure(new RuntimeException(message, cause)))
    }

    case OnPartFinished(streamStorage, headers) => {
      logger.info(s"Headers:  ${headers.asScala.map(x => s"${x._1} -> ${x._2.asScala.mkString(", ")}").mkString("\n")}")
      headers.get("filename").asScala.headOption match {
        case None => {
          onComplete.failure(new IllegalStateException(s"Couldn't read filename from headers: $headers"))
          context.stop(self)
        }
        case Some(filename) => {
          val bytes = IOUtils.toByteArray(streamStorage.getInputStream)
          AzureUploader.upload(filename, new ByteArrayInputStream(bytes), bytes.length)
        }
      }
    }

    case OnAllPartsFinished => {
      onComplete.success(Success(Unit))
    }
  }

  override def postStop(): Unit = {
    if (!onComplete.isCompleted) {
      onComplete.failure(new IllegalStateException("Actor stopped before finishing job"))
    }
    super.postStop()
  }
}

case class OnError(message: String, cause: Throwable)

case class OnPartFinished(partBodyStreamStorage: StreamStorage, headersFromPart: util.Map[String, util.List[String]])

case object OnAllPartsFinished

case object OnNestedPartFinished

case class OnNestedPartStarted(headersFromParentPart: util.Map[String, util.List[String]])