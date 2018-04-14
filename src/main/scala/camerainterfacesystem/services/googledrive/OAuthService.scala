package camerainterfacesystem.services.googledrive

import java.time.Instant

import camerainterfacesystem.Config
import camerainterfacesystem.services.AppService
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import okhttp3.{FormBody, Request, Response}

object OAuthService extends AppService {

  class PermissionException extends RuntimeException

  private val clientId = Config.config.getString("google.client_id")
  private val clientSecret = Config.config.getString("google.client_secret")
  private val redirectUri = Config.config.getString("google.redirect_uri")

  private case class TokenRequestResponse(
                                           @JsonProperty("access_token") accessToken: String,
                                           @JsonProperty("token_type") tokenType: String,
                                           @JsonProperty("expires_in") expiresIn: Int,
                                           @JsonProperty("refresh_token") refreshToken: String
                                         )

  def handleCode(code: String): AuthData = {
    logger.debug("Handling code")
    val formBody =
      buildCommonParams
        .add("code", code)
        .add("grant_type", "authorization_code")
        .build()

    val request = new Request.Builder()
      .url("https://www.googleapis.com/oauth2/v4/token")
      .post(formBody)
      .build()

    val requestTime = Instant.now()
    val response = Config.httpClient.newCall(request).execute()
    checkIfAuthProblem(response.code())
    if (response.isSuccessful) {
      val tokenRequestResponse = Config.objectMapper.readValue(response.body().bytes(), classOf[TokenRequestResponse])
      val expireDate = requestTime.plusSeconds(tokenRequestResponse.expiresIn)
      logger.debug(s"Successfully retrieved auth token lasting ${tokenRequestResponse.expiresIn}")
      AuthData(tokenRequestResponse.accessToken, expireDate, tokenRequestResponse.refreshToken)
    } else {
      logger.warn(s"Failed to retrieve code \n${response.body().string()}")
      throw new IllegalStateException(s"${response.code()} ${response.message()}")
    }

  }

  def refreshToken(authData: AuthData): AuthData = {
    logger.debug("Refreshing token")
    val formBody =
      buildCommonParams
        .add("grant_type", "refresh_token")
        .add("refresh_token", authData.refreshToken)
        .build()

    val request = new Request.Builder()
      .url("https://www.googleapis.com/oauth2/v4/token")
      .post(formBody)
      .build()

    val response = Config.httpClient.newCall(request).execute()
    val requestTime = Instant.now()
    checkIfAuthProblem(response.code())
    if (response.isSuccessful) {
      logger.debug("Refreshed token")
      val tokenRequestResponse = Config.objectMapper.readValue(response.body().bytes(), classOf[TokenRequestResponse])
      val expireDate = requestTime.plusSeconds(tokenRequestResponse.expiresIn)
      logger.debug(s"Token expire at ${expireDate} cause expiresIn is ${tokenRequestResponse.expiresIn} and requestTime is ${requestTime}")
      authData.copy(accessToken = tokenRequestResponse.accessToken, expireDate = expireDate)
    } else {
      throw new IllegalStateException(s"${response.code()} ${response.message()}")
    }
  }

  def performAroundToken[T](authData: AuthData, operation: (AuthData) => T): (T, AuthData) = {
    val validAuthData = if (authData.isExpired()) {
      logger.debug(s"Refresing token cause now is ${Instant.now()} and token expired at ${authData.expireDate}")
      refreshToken(authData)
    } else {
      logger.debug(s"Not refreshing token cause it is valid till ${authData.expireDate.toString}")
      authData
    }
    try {
      operation.apply(validAuthData) -> validAuthData
    } catch {
      case _: PermissionException => {
        val refreshedToken = refreshToken(authData)
        operation.apply(refreshedToken) -> refreshedToken
      }
    }
  }

  def checkIfAuthProblem(code: Int): Unit = {
    if (code == 400) {
      logger.error(s"Http error with code ${code}")
      throw new PermissionException
    }
  }

  private def buildCommonParams = {
    new FormBody.Builder()
      .add("client_id", clientId)
      .add("client_secret", clientSecret)
      .add("redirect_uri", redirectUri)
  }
}

case class AuthData(
                     accessToken: String,
                     expireDate: Instant,
                     refreshToken: String
                   ) {
  @JsonIgnore
  def isExpired(): Boolean = expireDate.isBefore(Instant.now())
}
