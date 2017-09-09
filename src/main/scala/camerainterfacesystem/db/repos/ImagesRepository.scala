package camerainterfacesystem.db.repos

import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.{DB, Tables}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object ImagesRepository extends SlickRepository {

  private val images = Tables.Images
  private val insertQuery = images returning images.map(_.id) into ((image, id) => image.copy(id = id))

  private val imageJoinPreset = for {
    (image, preset) <- images join Tables.Presets on (_.presetid === _.id)
  } yield (image, preset)

  def addImage(image: Image)(implicit executionContext: ExecutionContext): Future[Image] = {
    val query = for {
      _ <- images.filter(_.fullpath === image.fullpath).delete
      i <- insertQuery += image
    } yield i
    DB().run(query)
  }

  def addImages(imagesList: Seq[Image])(implicit executionContext: ExecutionContext): Future[Seq[Image]] = {
    val fullPaths = imagesList.map(_.fullpath).toSet
    val query = for {
      _ <- images.filter(_.fullpath.inSet(fullPaths)).delete
      i <- insertQuery ++= imagesList
    } yield i
    DB().run(query)
  }

  def getNewestImagesForPreset(presetArg: Preset, limit: Int): Future[Seq[(Image, Preset)]] = {
    require(limit >= 1, s"limit must be bigger or equals to 1: ${limit}")
    val query = imageJoinPreset
      .filter(_._2.id === presetArg.id)
      .sortBy(_._1.phototaken)
      .take(limit)

    DB().run(query.result)
  }

}
