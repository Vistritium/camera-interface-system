package camerainterfacesystem.db.repos

import java.time.Instant

import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.db.{DB, Tables}
import camerainterfacesystem.utils.CollectionUtils
import camerainterfacesystem.utils.CollectionUtils._
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}

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

  def getNewestImagesForPreset(presetId: Int, limit: Int): Future[(Seq[Image])] = {
    require(limit >= 1, s"limit must be bigger or equals to 1: ${limit}")
    val query = images
      .filter(_.presetid === presetId)
      .sortBy(_.phototaken.desc)
      .take(limit)

    DB().run(query.result)
  }

  def deleteImage(idOrFullpath: Either[Int, String])(implicit executionContext: ExecutionContext): Future[Unit] = {
    val query = (idOrFullpath match {
      case Left(id) => images.filter(_.id === id)
      case Right(value) => images.filter(_.fullpath === value)
    }).delete
    DB().run(query).map(_ => Unit)
  }

  def deleteAllBetween(min: Instant, max: Instant, dryRun: Boolean)(implicit executionContext: ExecutionContext): Future[Seq[Image]] = {
    DB().run {
      images
        .filter(_.phototaken > min)
        .filter(_.phototaken < max)
        .result
    } map { res =>
      if (!dryRun) {
        DB().run(images.filter(_.id inSet res.map(_.id).toSet).delete)
      }
      res
    }
  }

  def getImage(idOrFullpath: Either[Int, String]): Future[Option[Image]] = {
    val query = (idOrFullpath match {
      case Left(id) => images.filter(_.id === id)
      case Right(value) => images.filter(_.fullpath === value)
    }).result.headOption
    DB().run(query)
  }

  def getNewestImagesForAllPresets(hour: Int): Future[Vector[(Image, Preset)]] = {
    val sql =
      sql"""SELECT i1.*, presets.*
        FROM images i1
        LEFT JOIN images i2 ON i1.presetId == i2.presetId AND i1.photoTaken < i2.photoTaken AND i1.hourTaken = i2.hourTaken
        LEFT JOIN presets ON i1.presetId == presets.id
        WHERE i2.presetId IS NULL AND i1.hourTaken = $hour""".as[(Image, Preset)]

    DB().run(sql)
  }

  def getNewestImagesForAllPresets(): Future[Vector[(Image, Preset)]] = {

    val sql =
      sql"""SELECT i1.*, presets.*
    FROM images i1
    LEFT JOIN images i2 ON i1.presetId == i2.presetId AND i1.photoTaken < i2.photoTaken
    LEFT JOIN presets ON i1.presetId == presets.id
    WHERE i2.presetId IS NULL""".as[(Image, Preset)]

    DB().run(sql)
  }

  def getImagesForPresetAndHour(presetId: PresetId, hour: Hour,
    min: Option[Instant] = None,
    max: Option[Instant] = None)
    (implicit executionContext: ExecutionContext): Future[(Preset, Seq[Image])] = {
    PresetsRepository.getPresetById(presetId).flatMap {
      case None => throw new IllegalStateException("Unknown preset id")
      case Some(value) => {
        var query = imageJoinPreset
          .filter(x => x._2.id === presetId.presetId && x._1.hourTaken === hour.hour)
          .map(_._1)
        query = min match {
          case Some(min) => query.filter(_.phototaken >= min)
          case None => query
        }
        query = max match {
          case Some(max) => query.filter(_.phototaken <= max)
          case None => query
        }
        DB().run(query
          .result).map {
          value -> _
        }
      }
    }
  }

  def findImages(presets: Set[Int], hours: Set[Int], min: Instant, max: Instant, granulation: Int)(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset)]] = {
    val query = prepareFindImages(presets, hours, min, max, granulation)
    DB().run(query.result.map(res => {
      res
    }))
  }

  def findImagesCount(presets: Set[Int], hours: Set[Int], min: Instant, max: Instant, granulation: Int)(implicit executionContext: ExecutionContext): Future[Int] = {
    val mainQuery = prepareFindImages(presets, hours, min, max, granulation)
    val query = mainQuery.length
    DB().run(query.result)
      .map(CollectionUtils.granulation(_, granulation))
  }

  private def prepareFindImages(presets: Set[Int], hours: Set[Int], min: Instant, max: Instant, granulation: Int) = {
    imageJoinPreset
      .filter(_._2.id.inSet(presets))
      .filter(_._1.hourTaken.inSet(hours))
      .filter(_._1.phototaken >= min)
      .filter(_._1.phototaken <= max)
    /*.zipWithIndex
    .map(_._1)*/
  }

  def getClosestImagesToDates(dates: List[Instant], preset: Int)(implicit executionContext: ExecutionContext) = {
    val queries = dates.map { date =>
      val dateEpochMillis = date.toEpochMilli
      sql"""
        SELECT *
        FROM images
        WHERE presetId = 5
        ORDER BY abs(photoTaken - $dateEpochMillis) ASC
        LIMIT 1;
      """.as[Image].headOption.map(image => date -> image)
    }

    DB().run(DBIO.sequence(queries).transactionally)
  }

  def getAvailableHours(): Future[Seq[Int]] = {
    DB().run(
      images.groupBy(_.hourTaken).map(_._1).result
    )
  }

  def getEarliestDate(): Future[Option[Instant]] = {
    DB().run(images
      .map(_.phototaken)
      .sortBy(_.asc)
      .take(1)
      .result.headOption)
  }

  def getLatestDate(): Future[Option[Instant]] = {
    DB().run(images
      .map(_.phototaken)
      .sortBy(_.desc)
      .take(1)
      .result.headOption)
  }

  def countImagesBetweenDates(min: Instant, max: Instant): Future[Int] = {
    DB().run(images
      .filter(_.phototaken > min)
      .filter(_.phototaken < max)
      .length.result)
  }

}
