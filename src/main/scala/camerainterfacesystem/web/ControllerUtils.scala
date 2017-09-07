package camerainterfacesystem.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import play.twirl.api.BufferedContent
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute

trait ControllerUtils {
  protected val htmlToResponseMarshalable: (BufferedContent[_]) => StandardRoute = ControllerUtils.htmlToResponseMarshalable
}

object ControllerUtils {
  private val htmlToResponseMarshalable = (x: BufferedContent[_]) =>
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, x.body))
}