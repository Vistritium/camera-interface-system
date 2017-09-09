package camerainterfacesystem.web.controllers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import camerainterfacesystem.web.AppController

class HelloController extends AppController {
  override def route = pathSingleSlash {
    get {
      complete("Hello. App is working. Get lost :)")
    }
  }
}
