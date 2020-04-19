package camerainterfacesystem.utils

import java.time._
import java.time.format.DateTimeFormatter
import java.util.{Objects, TimeZone}

import camerainterfacesystem.configuration.AppConfig
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.lang3.StringUtils

import scala.util.{Failure, Success, Try}

@Singleton
class ImageUtils @Inject()(
  appConfig: AppConfig,
  config: Config
) extends LazyLogging {

  private val timezone = TimeZone.getTimeZone(config.getString("uploadedFormatTimezone"))

  def extractPropertiesFromPath(name: String): Try[ImagePathProperties] = {
    Try {
      val strings = name.split(' ').reverse
      require(strings.length >= 3, s"split filename must be 3 or bigger: ${strings.length} : ${name}")
      val hour = StringUtils.substringBeforeLast(strings(0), ".").toInt
      val dateTime = LocalDate.parse(strings(1), DateTimeFormatter.ofPattern("yyyy.MM.dd"))

      val fullPath = s"${dateTime.getYear}/${dateTime.getMonthValue}/${dateTime.getDayOfMonth}/${name}"

      val presetName = strings.reverse.take(strings.length - 2).mkString(" ")

      ImagePathProperties(name, fullPath, presetName, hour)
    }
  }

  private val exifDateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

  def extractDateFromJpeg(byteArray: Array[Byte]): Try[ZonedDateTime] = {
    Option(Imaging.getMetadata(byteArray))
      .map(_.asInstanceOf[JpegImageMetadata])
      .flatMap(x => Option(x.getExif)) match {
      case Some(exif) => {
        val strings = exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED)
        require(Objects.nonNull(strings) && strings.nonEmpty, "Found exif data but date is empty")
        val utcLocalDateTime = LocalDateTime.parse(strings.head, exifDateFormatter)
        Success(ZonedDateTime.of(utcLocalDateTime, timezone.toZoneId))
      }
      case None => Failure(new IllegalStateException("Couldn't extract metadata from jpg"))
    }
  }
}

case class ImagePathProperties(filename: String, fullpath: String, presetName: String, hourTaken: Int)