package camerainterfacesystem.web.controllers.rest

import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{HttpCookie, HttpCookiePair}
import akka.http.scaladsl.model.{DateTime, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import camerainterfacesystem.{Config, Main}
import camerainterfacesystem.db.repos.ImagesRepository
import camerainterfacesystem.services.{GetData, GetDataResult, ImagesService}
import camerainterfacesystem.services.googledrive.{AuthData, GoogleDriveUploadService, OAuthService}
import org.apache.commons.codec.binary.Base64
import akka.pattern.ask

class GoogleDriveController extends AppRestController {

  override def restRoute: Route = pathPrefix("googledrive") {
    get {
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
      }
    } ~ post {
      path("upload" / Segment) { path =>
        GoogleDriveController.extractAuthData {
          case Some(authData) => {
            handleFutureError(onComplete(ImagesRepository.getImage(Right(path)))) {
              case Some(image) => {
                handleFutureError(onComplete((Main.imageDataService ? GetData(image.fullpath)).mapTo[GetDataResult])) {
                  imageData => {
                    GoogleDriveController.updateAuthDataIfNeeded {
                      GoogleDriveUploadService.uploadImage(authData, image.filename, imageData.bytes)
                    } { _ =>
                      complete("ok")
                    }
                  }
                }
              }
              case None => {
                complete(HttpResponse(status = StatusCodes.NotFound, entity = s"Image of fullpath ${path} does not exist"))
              }
            }
          }
          case None => {
            redirectToLogin
          }
        }
      } ~ path("login") {
        redirectToLogin
      }
    }
    /*~ path("test") {
     GoogleDriveController.extractAuthData {
       case Some(authData) => {
         GoogleDriveController.updateAuthDataIfNeeded(GoogleDriveUploadService.uploadImage(authData)) {
           _ => complete("something happened")
         }
         //GoogleDriveUploadService.upload(authData, "test")
       }
       case None => complete("idiot no auth data, lmao")
     }
   }*/
  }


  val redirectToLogin = redirect(
    Uri("https://accounts.google.com/o/oauth2/v2/auth")
      .withQuery(
        Query(Map(
          "scope" -> "https://www.googleapis.com/auth/drive.file",
          "access_type" -> "offline",
          "include_granted_scopes" -> "true",
          "state" -> "/",
          "redirect_uri" -> Config().getString("google.redirect_uri"),
          "response_type" -> "code",
          "client_id" -> Config().getString("google.client_id"),
          "prompt" -> "consent"
        ))),
    StatusCodes.TemporaryRedirect
  )


}

object GoogleDriveController {
  val AuthDataCookieName = "innocentcookie"

  private def getCookie(data: AuthData): HttpCookie = {
    HttpCookie(
      GoogleDriveController.AuthDataCookieName,
      Base64.encodeBase64URLSafeString(Config.objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8)),
      Some(DateTime.MaxValue),
      httpOnly = true,
      path = Some("/")
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

  def updateAuthDataIfNeeded[T](dataa: (T, AuthData)): Directive1[T] = {
    setCookie(getCookie(dataa._2)).tflatMap((_) => provide(dataa._1))
  }
}
