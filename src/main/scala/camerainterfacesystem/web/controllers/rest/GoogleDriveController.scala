package camerainterfacesystem.web.controllers.rest

import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.headers.{HttpCookie, HttpCookiePair}
import akka.http.scaladsl.model.{DateTime, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import camerainterfacesystem.Config
import camerainterfacesystem.services.googledrive.{AuthData, OAuthService}
import org.apache.commons.codec.binary.Base64

class GoogleDriveController extends AppRestController {

  override def restRoute: Route = pathPrefix("googledrive") {
    path("redirect") {
      parameters("state", "code") {
        (redirectUrl, code) => {
          val data = OAuthService.handleCode(code)
          setCookie(GoogleDriveController.getCookie(data)) {
            redirect(Uri(redirectUrl), StatusCodes.TemporaryRedirect)
          }
        }
      }
    } ~ path("check") {
      GoogleDriveController.extractAuthData {
        case Some(_) => complete("yes")
        case None => complete("no")
      }
    } ~ path("upload" / LongNumber) { id =>
      optionalCookie(GoogleDriveController.AuthDataCookieName) {
        case Some(HttpCookiePair(_, value)) => {
          val authData = Config.objectMapper.readValue(value, classOf[AuthData])
          logger.info(authData.toString)
          complete(s"Let's pretend that it succeded | \n ${value}")
        }
        case None => {
          val erorMsg = new {
            val errorCode = "AUTH_DATA_NOT_EXIST"
          }
          complete(HttpResponse(StatusCodes.Conflict, entity = Config.objectMapper.writeValueAsString(erorMsg)))
        }
      }
    }
  }


}

object GoogleDriveController {
  val AuthDataCookieName = "innocentcookie"

  def getCookie(data: AuthData): HttpCookie = {
    HttpCookie(
      GoogleDriveController.AuthDataCookieName,
      Base64.encodeBase64URLSafeString(Config.objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8)),
      Some(DateTime.MaxValue),
      httpOnly = true
    )
  }

  def extractAuthData: Directive1[Option[AuthData]] = {
    optionalCookie(AuthDataCookieName).tmap(a => {
      a._1.map {
        case HttpCookiePair(_, value) => {
          val decodedValue = new String(Base64.decodeBase64(value), StandardCharsets.UTF_8)
          Config.objectMapper.readValue(decodedValue, classOf[AuthData])
        }
      }
    })
  }
}
