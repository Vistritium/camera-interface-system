package camerainterfacesystem.db.repos

import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.util.{Count, Hour, PresetId}
import camerainterfacesystem.db.{DB, Tables}
import camerainterfacesystem.utilmodel.HoursImageCount
import slick.dbio.DBIOAction
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object PresetsRepository extends SlickRepository {

  private val presets = Tables.Presets
  private val inserQuery = presets returning presets.map(_.id) into ((preset, id) => preset.copy(id = id))

  private val presetJoinImages = for {
    (preset, image) <- presets join Tables.Images on (_.id === _.presetid)
  } yield (preset, image)

  def addPreset(preset: Preset): Future[Preset] = {
    val query = inserQuery += preset
    DB().run(query)
  }

  def getPresetsForImage(image: Image): Future[Seq[Preset]] = {
    val query = presets.filter(_.id === image.id)
    DB().run(query.result)
  }

  def findPresetByName(name: String): Future[Option[Preset]] = {
    val query = presets.filter(_.name === name)
    DB().run(query.result.headOption)
  }

  def getPresetById(id: PresetId): Future[Option[Preset]] =
    DB().run(presets.filter(_.id === id.presetId).result.headOption)

  def findPresetByNameOrCreateNew(name: String)(implicit executionContext: ExecutionContext): Future[Preset] = {
    val query = presets.filter(_.name === name).result.headOption.flatMap {
      case Some(value) => DBIOAction.successful(value)
      case None => inserQuery += Preset(0, name, None)
    }.withPinnedSession

    val eventualResult: Future[Preset] = DB().run(query)
    eventualResult
  }

  def getAllPresetsGroupedByHour()(implicit executionContext: ExecutionContext): Future[Seq[(Preset, Count, Hour)]] = {
    DB().run {
      sql"""
            SELECT presets.*, COUNT(images.id), images.hourTaken FROM presets
            LEFT JOIN images ON presets.id = images.presetId
            GROUP BY presets.id, images.hourTaken
        """.as[(Preset, Int, Int)]
    }.map(x => x.map(r => (r._1, Count(r._2), Hour(r._3))))
  }

  def getPresetsHours(presetId: Int)(implicit executionContext: ExecutionContext): Future[Seq[HoursImageCount]] = {
    DB().run(
      sql"""
            SELECT images.hourTaken, count(images.id) FROM presets
            LEFT JOIN images ON presets.id = images.presetId
            WHERE presetId = $presetId
            GROUP BY images.hourTaken
        """.as[(Int, Int)])
      .map(_.map(Function.tupled(HoursImageCount.apply)))
  }


}
