package camerainterfacesystem.web.controllers.gcodeparser

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import camerainterfacesystem.Config
import camerainterfacesystem.web.AppController

import scala.collection.JavaConverters._

class GCodeParserController extends AppController {

  def decode(gcode: String): String = {
    val lines = gcode.replace("\r", "").split("\n")
    val stringToTrim = ";SETTING_3"
    val res = lines
      .filter(_.startsWith(stringToTrim))
      .map(_.substring(stringToTrim.length + 1))
      .reduceLeft(_ + _)

    val node = Config.objectMapper.readTree(res)

    def parseIni(name: String, string: String): String = {
      val replaced = string.replace("""\n""", "\n")
      s"### $name ###\n$replaced"
    }

    val result = node.fields().asScala.flatMap(node => {
      if (node.getValue.isArray) {
        node.getValue.elements().asScala.zipWithIndex.map {
          case (elem, index) => s"${node.getKey}_$index" -> elem.textValue()
        }
      } else {
        List(node.getKey -> node.getValue.textValue())
      }
    })
      .map(Function.tupled(parseIni))
      .mkString("\n")


    result
  }

  override def route: Route = pathPrefix("gcode") {
    path("parse") {
      post {
        formField("file" ?) { file =>
          if (file.isDefined && file.get.nonEmpty) {
            complete(decode(file.get))
          } else {
            formField("text") { text =>
              complete(decode(text))
            }
          }
        }
      }
    } ~ get {
      htmlToResponseMarshalable(gcodeparser.html.gcodeparser.render())
    }

  }
}
