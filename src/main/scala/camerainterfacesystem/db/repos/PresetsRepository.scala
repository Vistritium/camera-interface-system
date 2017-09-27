package camerainterfacesystem.db.repos

import camerainterfacesystem.db.{DB, Tables}
import camerainterfacesystem.db.Tables.{Image, Preset}
import camerainterfacesystem.db.util.Count
import slick.dbio.DBIOAction
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object PresetsRepository extends SlickRepository {

  private val presets = Tables.Presets
  private val inserQuery = presets returning presets.map(_.id) into ((preset, id) => preset.copy(id = id))

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

  def findPresetByNameOrCreateNew(name: String)(implicit executionContext: ExecutionContext): Future[Preset] = {
    val query = presets.filter(_.name === name).result.headOption.flatMap {
      case Some(value) => DBIOAction.successful(value)
      case None => inserQuery += Preset(0, name, None)
    }.withPinnedSession

    val eventualResult: Future[Preset] = DB().run(query)
    eventualResult
  }

  def getAllPresets()(implicit executionContext: ExecutionContext): Future[Seq[(Preset, Count)]] = {
    DB().run {
      sql"""
            SELECT presets.*, COUNT(images.id) FROM presets
            LEFT JOIN images ON presets.id = images.presetId
            GROUP BY presets.id
        """.as[(Preset, Int)]
    }.map(x => x.map(r => r._1 -> Count(r._2)))
  }


}
