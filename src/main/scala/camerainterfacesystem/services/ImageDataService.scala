package camerainterfacesystem.services

import camerainterfacesystem.{AppActor, Config}
import camerainterfacesystem.azure.Azure
import com.github.blemale.scaffeine
import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.Future

class ImageDataService extends AppActor {

  val cache: scaffeine.Cache[String, Array[Byte]] =
    Scaffeine.apply()
      .maximumWeight(Config().getInt("cache.inMemoryImageCache"))
      .weigher(((_, v) => v.length): (String, Array[Byte]) => Int)
      .build()


  override def receive: Receive = {

    case GetData(path) => {
      replyAsk(sender(), Future {
        cache.get(path, _ => Azure.download(path))
      }.map(GetDataResult))
    }
    case CacheData(path) => {
      Future {
        cache.get(path, _ => Azure.download(path))
      }
    }
  }

}

case class GetData(fullpath: String)

case class CacheData(fullpath: String)

case class GetDataResult(bytes: Array[Byte])
