package camerainterfacesystem.utils

import scala.concurrent.{ExecutionContext, Future}

object FunctionalUtils {

  def reverseEitherFuture[A, B](either: Either[A, Future[B]])(implicit context: ExecutionContext): Future[Either[A, B]] = {
    either match {
      case left: Left[_, _] => Future.successful(left).asInstanceOf[Future[Left[A, B]]]
      case Right(value) => value.map(Right(_))
    }
  }

}
