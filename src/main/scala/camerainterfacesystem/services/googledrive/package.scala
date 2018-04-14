package camerainterfacesystem.services

import okhttp3.Request

package object googledrive {

  implicit class RichBuilder(builder: Request.Builder) {
    def addAuthHeader(authData: AuthData): Request.Builder =
      builder.addHeader("Authorization", s"Bearer ${authData.accessToken}")
    
  }


}
