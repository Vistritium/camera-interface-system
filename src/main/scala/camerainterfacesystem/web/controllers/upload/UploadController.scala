package camerainterfacesystem.web.controllers.upload

import akka.actor.Props
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import camerainterfacesystem.web.{AppController, WebServer}
import org.synchronoss.cloud.nio.multipart.{Multipart, MultipartContext}

import scala.concurrent.{ExecutionContextExecutor, Promise}
import scala.util.{Failure, Success, Try}

class UploadController extends AppController  {

  private implicit val materializer: ActorMaterializer = WebServer.webMaterializer
  private implicit val dispatcher: ExecutionContextExecutor = materializer.system.dispatcher

  override def route: Route = path("upload") {
    post {
      extractRequest { req =>
        val onCompleteTry: Promise[Try[Unit]] = Promise[Try[Unit]]()

        val ref = materializer.system.actorOf(Props(classOf[UploadReceiverActor], onCompleteTry))
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
          case Success(tryy) => tryy match {
            case Failure(exception) => complete(HttpResponse(status = StatusCodes.UnprocessableEntity, entity = exception.getMessage))
            case Success(_) => complete(HttpResponse(StatusCodes.NoContent))
          }
          case Failure(exception) => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = exception.getMessage))
        }
      }
    }
  }

}


