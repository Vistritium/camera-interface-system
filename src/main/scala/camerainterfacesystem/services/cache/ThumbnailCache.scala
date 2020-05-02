package camerainterfacesystem.services.cache

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import camerainterfacesystem.configs.{DBConfig, LocalStorageConfig}
import camerainterfacesystem.services.akkap.ImageDataService
import camerainterfacesystem.services.{AkkaRefNames, AppService, ThumbnailMaker}
import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.mapdb.{DBMaker, HTreeMap, Serializer}

import scala.concurrent.Future
import scala.concurrent.duration._


@Singleton
class ThumbnailCache @Inject()(
  localStorageConfig: LocalStorageConfig,
  config: Config,
  override protected implicit val system: ActorSystem,
  @Named(AkkaRefNames.ImageDataService) imageDataService: ActorRef[ImageDataService.Command],
  thumbnailMaker: ThumbnailMaker,
) extends AppService with LazyLogging {


  private val mapdbHolder = DBMaker.fileDB(localStorageConfig.path.resolve("mapdb").toFile)
    .checksumHeaderBypass
    .closeOnJvmShutdown()
    .make()
  private val mapdb: HTreeMap[String, Array[Byte]] = mapdbHolder
    .hashMap[String, Array[Byte]]("thumbnail", Serializer.STRING, Serializer.BYTE_ARRAY).createOrOpen()

  Source.tick(2.seconds, 2.seconds, "tick")
    .runForeach(_ => mapdbHolder.commit)

  private val cache: AsyncLoadingCache[String, ByteString] =
    Scaffeine.apply()
      .maximumWeight(config.getInt("cache.inMemoryThumbnailCache"))
      .weigher(((_, v) => v.length): (String, ByteString) => Int)
      .buildAsyncFuture[String, ByteString](fullpath => {
        val bytes: Array[Byte] = mapdb.get(fullpath)
        Option(bytes)
          .map { r: Array[Byte] =>
            logger.info(s"mapdb hit - $fullpath")
            Future.successful(r)
          }
          .getOrElse {
            logger.info(s"azure hit - $fullpath")
            imageDataService.ask[ImageDataService.GetDataResult](r => ImageDataService.GetData(fullpath, r)).map(_.bytes)
              .map { r =>
                val thumbnail = thumbnailMaker.make(r)
                mapdb.put(fullpath, thumbnail)

                thumbnail
              }

          }.map(x => ByteString.apply(x))
      }
      )

  def getThumbnails(paths: List[String]): Future[Map[String, ByteString]] = cache.getAll(paths)

  def getThumbnail(fullpath: String): Future[ByteString] = cache.get(fullpath).map(ByteString.apply)

  def cache(fullpath: String, data: ByteString): Unit = {
    mapdb.put(fullpath, data.toArray)
    getThumbnail(fullpath)
  }

}
