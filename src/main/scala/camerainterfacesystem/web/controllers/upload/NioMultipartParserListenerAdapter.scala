package camerainterfacesystem.web.controllers.upload

import java.util

import akka.actor.ActorRef
import org.synchronoss.cloud.nio.multipart.NioMultipartParserListener
import org.synchronoss.cloud.nio.stream.storage.StreamStorage

class NioMultipartParserListenerAdapter(ref: ActorRef) extends NioMultipartParserListener {
  override def onError(message: String, cause: Throwable): Unit =
    ref ! OnError(message, cause)

  override def onPartFinished(partBodyStreamStorage: StreamStorage, headersFromPart: util.Map[String, util.List[String]]): Unit =
    ref ! OnPartFinished(partBodyStreamStorage, headersFromPart)

  override def onAllPartsFinished(): Unit = ref ! OnAllPartsFinished

  override def onNestedPartFinished(): Unit = ref ! OnNestedPartFinished

  override def onNestedPartStarted(headersFromParentPart: util.Map[String, util.List[String]]): Unit =
    ref ! OnNestedPartStarted(headersFromParentPart)
}
