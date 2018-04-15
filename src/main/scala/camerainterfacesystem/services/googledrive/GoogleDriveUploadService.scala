package camerainterfacesystem.services.googledrive

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import camerainterfacesystem.Config
import camerainterfacesystem.services.googledrive.OAuthService.PermissionException
import com.typesafe.scalalogging.LazyLogging
import okhttp3.{MediaType, Request, RequestBody}
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder


object GoogleDriveUploadService extends LazyLogging {

  private val baseUrl = "https://www.googleapis.com"
  private val directoryName = Config.config.getString("google.directoryName")

  case class FilesResponseElement(id: String, name: String, mimeType: String, kind: String)
  case class FilesResponse(files: List[FilesResponseElement])

  def uploadImage(authData: AuthData, filename: String, data: Array[Byte]): (Unit, AuthData) = {
    searchForImagesDirectory(authData) match {
      case (filesResponse, authData) => {
        val (id, authDataRefreshed) = if (filesResponse.files.isEmpty) {
          logger.info("App directory does not exist, creating")
          val created = createAppDirectory(authData)
          created._1.id -> created._2
        } else {
          logger.info(s"woo, file exist: ${filesResponse.files.head}")
          filesResponse.files.head.id -> authData
        }
        uploadImageToDirectory(authDataRefreshed, id, filename, data)
      }
    }
  }

  private def uploadImageToDirectory(authData: AuthData, parentId: String, filename: String, data: Array[Byte]): (Unit, AuthData) = {
    OAuthService.performAroundToken(authData, (authData) => {
      val metadata = new {
        val name = filename
        val parents = List(parentId)
      }

      val build = MultipartEntityBuilder.create()
        //.setStrictMode()
        .setBoundary("1")
        .addTextBody("1", Config.objectMapper.writeValueAsString(metadata), ContentType.parse("application/json; charset=UTF-8"))
        .addBinaryBody("1", data)
        .build()
      val stream = new ByteArrayOutputStream(data.size + 1000)
      build.writeTo(stream)

      val array = stream.toByteArray
      logger.debug(s"Multipart: \n${new String(array)}")


      val post = new HttpPost(s"$baseUrl/upload/drive/v3/files?uploadType=multipart")
      post.addHeader("Authorization", s"Bearer ${authData.accessToken}")
      post.setEntity(build)

      val response = Config.apacheHttpClient.execute(post)

      val body = IOUtils.toString(response.getEntity.getContent, StandardCharsets.UTF_8)
      if (response.getStatusLine.getStatusCode.toString.startsWith("2")) {

        logger.debug(s"upload success:\n${body}")
        () -> authData
      } else if (response.getStatusLine.getStatusCode == 400) {
        throw new PermissionException()
      } else {
        throw new IllegalStateException(s"${body}")
      }
    })
  }

  private def searchForImagesDirectory(authData: AuthData): (FilesResponse, AuthData) = {
    OAuthService.performAroundToken(authData, (authData) => {
      val uri = Uri(s"$baseUrl/drive/v3/files")
        .withQuery(Query(Map(
          "q" -> s"mimeType = 'application/vnd.google-apps.folder' and trashed = false and parents in 'root' and name = '$directoryName'"
        )))

      val request = new Request.Builder()
        .url(uri.toString())
        .get()
        .addAuthHeader(authData)
        .build()

      val call = Config.httpClient.newCall(request).execute()
      OAuthService.checkIfAuthProblem(call.code())
      if (call.isSuccessful) {
        Config.objectMapper.readValue(call.body().string(), classOf[FilesResponse])
      } else {
        throw new IllegalStateException(s"${call.body().string()}")
      }
    })
  }

  private def createAppDirectory(authData: AuthData): (FilesResponseElement, AuthData) = {

    OAuthService.performAroundToken(authData, (authData) => {
      val metadata = new {
        val name = directoryName
        val mimeType = "application/vnd.google-apps.folder"
      }

      val request = new Request.Builder()
        .url(s"$baseUrl/drive/v3/files")
        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
          Config.objectMapper.writeValueAsString(metadata)))
        .addAuthHeader(authData)
        .build()

      val call = Config.httpClient.newCall(request).execute()
      OAuthService.checkIfAuthProblem(call.code())

      if (call.isSuccessful) {
        Config.objectMapper.readValue(call.body().string(), classOf[FilesResponseElement])
      } else {
        throw new IllegalStateException(s"${call.body().string()}")
      }
    })
  }

}
