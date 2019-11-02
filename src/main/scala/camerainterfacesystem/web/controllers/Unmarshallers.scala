package camerainterfacesystem.web.controllers

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.{Instant, OffsetDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.unmarshalling.{PredefinedFromStringUnmarshallers, Unmarshaller}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

object Unmarshallers {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  val instantUnmarshaller: Unmarshaller[String, Instant] = Unmarshaller.strict(s => {
    OffsetDateTime.parse(s, dateTimeFormatter).toInstant
  })

  val commaSeparatedEpochDates: Unmarshaller[String, Seq[Instant]] = Unmarshaller.strict {
    case s if s.isEmpty => Seq.empty
    case s => s.split(",").map(_.toLong).map(Instant.ofEpochMilli)
  }

  val seqStringUnmarshaller: Unmarshaller[String, Seq[String]] = new Unmarshaller[String, Seq[String]] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[Seq[String]] = {
      Future {
        if (value.isEmpty) Seq()
        else
          value.split(",").toSeq
      }(materializer.executionContext)
    }
  }

  val seqIntUnmarshaller: Unmarshaller[String, Seq[Int]] = seqStringUnmarshaller.map(_.map(_.toInt))

}
