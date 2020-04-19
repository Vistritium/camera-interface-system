package camerainterfacesystem.web.controllers.upload

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import camerainterfacesystem.utils.StoppingSupervisor
import camerainterfacesystem.web.AppController
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.synchronoss.cloud.nio.multipart.{Multipart, MultipartContext}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Promise}
import scala.util.{Failure, Success}

@Singleton
class UploadController @Inject()(
  implicit val system: ActorSystem,
  protected val executionContext: ExecutionContext,
  uploadReceiverActorFactory: UploadReceiverActorFactory
) extends AppController with  LazyLogging {


  override def route: Route = path("upload") {
    post {
      extractRequest { req =>
        val onCompleteTry: Promise[Unit] = Promise[Unit]()

        val ref = system.actorOf(StoppingSupervisor(Props(uploadReceiverActorFactory.uploadReceiverActor(onCompleteTry))))
        val adapter = new NioMultipartParserListenerAdapter(ref)

        val contentLength = req.entity.contentLengthOption.getOrElse(-1.toLong).toInt
        val parser = Multipart.multipart(new MultipartContext(req.entity.contentType.value,
          contentLength, req.encoding.value))
          .forNIO(adapter)

        req.entity.dataBytes.runForeach(str => {
          val bytes = str.toByteBuffer.array()
          parser.write(bytes)
        })

        onComplete(onCompleteTry.future) {
          case Success(tryy) => {
            logger.info("Successfully Received files")
            complete(HttpResponse(StatusCodes.NoContent))
          }
          case Failure(exception) => {
            logger.warn(s"Upload finished with error: ${exception.getMessage}")
            logger.debug(s"Upload finished with error: ${exception.getMessage}", exception)
            complete(HttpResponse(status = StatusCodes.InternalServerError, entity = exception.getMessage))
          }
        }
      }
    }
  }

}


