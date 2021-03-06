package camerainterfacesystem.db

import java.sql.Timestamp
import java.time.{Instant, OffsetDateTime}

import slick.ast.BaseTypedType
import slick.jdbc.{GetResult, JdbcType}
import camerainterfacesystem.db.AppPostgresProfile.api._
import slick.model.ForeignKeyAction

object Tables {

  implicit def timestampToInstant(timestamp: Timestamp): Instant = if (timestamp == null) null else Instant.ofEpochMilli(timestamp.getTime)

  implicit val instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    { instant =>
      if (instant == null) null else new Timestamp(instant.toEpochMilli)
    },
    timestampToInstant
  )

  case class Image(id: Int, fullpath: String, filename: String, phototaken: OffsetDateTime, presetid: Int, hourTaken: Int)

  class Images(_tableTag: Tag) extends Table[Image](_tableTag, "images") {
    def * = (id, fullpath, filename, phototaken, presetid, hourTaken) <> (Image.tupled, Image.unapply)

    def ? = (id, Rep.Some(fullpath), Rep.Some(filename), Rep.Some(phototaken), Rep.Some(presetid), Rep.Some(hourTaken)).shaped.<>({ r => import r._; _2.map(_ => Image.tupled((_1, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val fullpath: Rep[String] = column[String]("full_path")
    val filename: Rep[String] = column[String]("file_name")
    val phototaken: Rep[OffsetDateTime] = column[OffsetDateTime]("photo_taken")
    val presetid: Rep[Int] = column[Int]("preset_id")
    val hourTaken: Rep[Int] = column[Int]("hour_taken")

    lazy val presetsFk = foreignKey("presets_FK_1", Rep.Some(presetid), Presets)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }


  implicit val getTzTimestamp: GetResult[OffsetDateTime] = GetResult(r => {
    val ret = date2TzTimestampTypeMapper.getValue(r.rs, r.currentPos + 1)
    r.skip
    ret
  })
  implicit val getImageResult: GetResult[Image] = GetResult(r => Image(r.<<, r.<<, r.<<, r.<<[OffsetDateTime], r.<<, r.<<))

  lazy val Images = new TableQuery(tag => new Images(tag))


  case class Preset(id: Int, name: String, displayname: Option[String]) {
    def normalizeName: Preset = displayname match {
      case Some(_) => this
      case None => this.copy(displayname = Some(name))
    }
  }

  class Presets(_tableTag: Tag) extends Table[Preset](_tableTag, "presets") {
    def * = (id, name, displayname) <> (Preset.tupled, Preset.unapply)

    def ? = (id, Rep.Some(name), Rep.Some(displayname)).shaped.<>({ r => import r._; _2.map(_ => Preset.tupled((_1, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val name: Rep[String] = column[String]("name")
    val displayname: Rep[Option[String]] = column[Option[String]]("display_name")
  }

  implicit val getPresetResult: GetResult[Preset] = GetResult(r => Preset(r.<<, r.<<, r.<<))

  lazy val Presets = new TableQuery(tag => new Presets(tag))

}
