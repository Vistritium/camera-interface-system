package camerainterfacesystem.db.repos

import java.time.Instant

import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.util.{Hour, PresetId}
import camerainterfacesystem.db.{DB, Tables}
import camerainterfacesystem.utils.CollectionUtils
import camerainterfacesystem.utils.CollectionUtils._
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable

@Singleton
class ImagesRepository @Inject()(
  protected val db: DB,
  presetsRepository: PresetsRepository,
) extends SlickRepository with LazyLogging {

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
    db().run(query)
  }

  def addImages(imagesList: Seq[Image])(implicit executionContext: ExecutionContext): Future[Seq[Image]] = {
    val fullPaths = imagesList.map(_.fullpath).toSet
    val query = for {
      _ <- images.filter(_.fullpath.inSet(fullPaths)).delete
      i <- insertQuery ++= imagesList
    } yield i
    db().run(query)
  }

  def getNewestImagesForPreset(presetId: Int, limit: Int): Future[(Seq[Image])] = {
    require(limit >= 1, s"limit must be bigger or equals to 1: ${limit}")
    val query = images
      .filter(_.presetid === presetId)
      .sortBy(_.phototaken.desc)
      .take(limit)

    db().run(query.result)
  }

  def deleteImage(idOrFullpath: Either[Int, String])(implicit executionContext: ExecutionContext): Future[Unit] = {
    val query = (idOrFullpath match {
      case Left(id) => images.filter(_.id === id)
      case Right(value) => images.filter(_.fullpath === value)
    }).delete
    db().run(query).map(_ => ())
  }

  def deleteAllBetween(min: Instant, max: Instant, dryRun: Boolean)(implicit executionContext: ExecutionContext): Future[Seq[Image]] = {
    db().run {
      images
        .filter(_.phototaken > min)
        .filter(_.phototaken < max)
        .result
    } map { res =>
      if (!dryRun) {
        db().run(images.filter(_.id inSet res.map(_.id).toSet).delete)
      }
      res
    }
  }

  def getImage(idOrFullpath: Either[Int, String]): Future[Option[Image]] = {
    val query = (idOrFullpath match {
      case Left(id) => images.filter(_.id === id)
      case Right(value) => images.filter(_.fullpath === value)
    }).result.headOption
    db().run(query)
  }

  def getNewestImagesForAllPresets(hour: Int): Future[Vector[(Image, Preset)]] = {
    val sql =
      sql"""SELECT i1.*, presets.*
        FROM images i1
        LEFT JOIN images i2 ON i1.presetId == i2.presetId AND i1.photoTaken < i2.photoTaken AND i1.hourTaken = i2.hourTaken
        LEFT JOIN presets ON i1.presetId == presets.id
        WHERE i2.presetId IS NULL AND i1.hourTaken = $hour""".as[(Image, Preset)]

    db().run(sql)
  }

  def getNewestImagesForAllPresets(): Future[Vector[(Image, Preset)]] = {

    val sql =
      sql"""SELECT i1.*, presets.*
    FROM images i1
    LEFT JOIN images i2 ON i1.presetId == i2.presetId AND i1.photoTaken < i2.photoTaken
    LEFT JOIN presets ON i1.presetId == presets.id
    WHERE i2.presetId IS NULL""".as[(Image, Preset)]

    db().run(sql)
  }

  def getImagesForPresetAndHour(
    presetId: PresetId, hour: Hour,
    min: Option[Instant] = None,
    max: Option[Instant] = None)
    (implicit executionContext: ExecutionContext): Future[(Preset, Seq[Image])] = {
    presetsRepository.getPresetById(presetId).flatMap {
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
        db().run(query
          .result).map {
          value -> _
        }
      }
    }
  }

  def findImages(presets: Set[Int], hours: Set[Int], min: Instant, max: Instant, granulation: Int)(implicit executionContext: ExecutionContext): Future[Seq[(Image, Preset)]] = {
    val queries = for {
      preset <- presets
      hour <- hours
    } yield findImages(preset, hour, min, max, granulation)
    Future.sequence(queries.map(q => db().run(q)).toList)
      .map(_.flatten)
  }

  def findImagesCount(presets: Set[Int], hours: Set[Int], min: Instant, max: Instant, granulation: Int)(implicit executionContext: ExecutionContext): Future[Int] = {
    if (granulation < 0) {
      val queries = for {
        preset <- presets
        hour <- hours
      } yield {
        logger.info(s"findImages($preset, $hour, $min, $max, $granulation)")
        findImages(preset, hour, min, max, granulation)
      }
      Future.sequence(queries.toList.map(q => db().run(q).map(_.length))).map(_.sum)
    } else {
      val queries = for {
        preset <- presets
        hour <- hours
      } yield prepareFindImages(preset, hour, min, max)
      Future.sequence(queries.map(q => db().run(q.length.result)).toList)
        .map { r =>
          r.map { c =>
            if (granulation == 0) c
            else (c / granulation) + 2
          }.sum
        }
    }
  }

  private def prepareFindImages(preset: Int, hour: Int, min: Instant, max: Instant) = {
    imageJoinPreset
      .filter(_._1.presetid === preset)
      .filter(_._1.hourTaken === hour)
      .filter(_._1.phototaken >= min)
      .filter(_._1.phototaken <= max)
  }

  private def findImages(preset: Int, hour: Int, min: Instant, max: Instant, granulation: Int)
    (implicit executionContext: ExecutionContext) = {

    val base = prepareFindImages(preset, hour, min, max)

    if (granulation < 0) {
      (base.sortBy(_._1.phototaken.asc).take(1) unionAll base.sortBy(_._1.phototaken.desc).take(1)).result
    } else {
      base
        .result
        .map { r =>
          val sorted = r.sortBy(_._1.phototaken)
          val effectiveGranulation = if (granulation == 0) 1 else granulation
          (sorted.grouped(effectiveGranulation).map(_.head).toSet + r.head + r.last).toSeq.sortBy(_._1.phototaken)
        }
    }

  }

  def getClosestImagesToDates(dates: List[Instant], preset: Int)(implicit executionContext: ExecutionContext)
  : Future[List[(Instant, Option[Image])]] = {
    val queries = dates.map { date =>
      val dateEpochMillis = date.toEpochMilli
      sql"""
        SELECT *
        FROM images
        WHERE presetId = $preset
        ORDER BY abs(photoTaken - $dateEpochMillis) ASC
        LIMIT 1;
      """.as[Image].headOption.map(image => date -> image)
    }

    db().run(DBIO.sequence(queries).transactionally)
  }

  def getAvailableHours(): Future[Seq[Int]] = {
    db().run(
      images.groupBy(_.hourTaken).map(_._1).result
    )
  }

  def getEarliestDate(): Future[Option[Instant]] = {
    db().run(images
      .map(_.phototaken)
      .sortBy(_.asc)
      .take(1)
      .result.headOption)
  }

  def getLatestDate(): Future[Option[Instant]] = {
    db().run(images
      .map(_.phototaken)
      .sortBy(_.desc)
      .take(1)
      .result.headOption)
  }

  def countImagesBetweenDates(min: Instant, max: Instant): Future[Int] = {
    db().run(images
      .filter(_.phototaken > min)
      .filter(_.phototaken < max)
      .length.result)
  }

}
