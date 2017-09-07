package camerainterfacesystem.web.controllers

import camerainterfacesystem.web.AppController
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import camerainterfacesystem.web.AppController
import StatusCodes._
import Directives._

class HelloController extends AppController {
  override def route = pathSingleSlash {
    get {
      complete("Hello. App is working. Get lost :)")
    }
  }
}
