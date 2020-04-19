package camerainterfacesystem.web.controllers.upload

import scala.concurrent.Promise

trait UploadReceiverActorFactory {

  def uploadReceiverActor(onComplete: Promise[Unit]): UploadReceiverActor

}
