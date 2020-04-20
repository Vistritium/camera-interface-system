package camerainterfacesystem.services.akkap

import akka.actor.typed._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import camerainterfacesystem.azure.Azure
import camerainterfacesystem.services.akkap.ImageDataService.{CacheData, Command, GetData, GetDataResult}
import com.github.blemale.scaffeine
import com.github.blemale.scaffeine.Scaffeine
import com.google.inject.assistedinject.Assisted
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

object ImageDataService {

  sealed trait Command

  case class GetData(fullpath: String, replyTo: ActorRef[GetDataResult]) extends Command
  case class CacheData(fullpath: String) extends Command
  case class GetDataResult(bytes: Array[Byte])

}

class ImageDataService @Inject()(
  @Assisted context: ActorContext[Command],
  config: Config,
  azure: Azure
) extends AbstractBehavior[Command](context) with LazyLogging {

  private val cache: scaffeine.Cache[String, Array[Byte]] =
    Scaffeine.apply()
      .maximumWeight(config.getInt("cache.inMemoryImageCache"))
      .weigher(((_, v) => v.length): (String, Array[Byte]) => Int)
      .build()

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case GetData(path, replyTo) => {
      replyTo ! GetDataResult(cache.get(path, _ => download(path)))
      Behaviors.same
    }
    case CacheData(path) => {
      cache.get(path, _ => download(path))
      Behaviors.same
    }
  }

  private def download(path: String) = {
    logger.info(s"Downloading image ${path} from cloud storage")
    azure.download(path)
  }
}


