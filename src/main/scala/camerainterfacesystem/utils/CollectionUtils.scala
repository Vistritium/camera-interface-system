package camerainterfacesystem.utils

import scala.collection.TraversableOnce

object CollectionUtils {

  implicit class RichCollection[+A](trav: TraversableOnce[A]) {
    def skip(n: Int): List[A] = {
      trav.foldLeft((List[A](), n)) { case ((acc, counter), x) =>
        if (counter == 1)
          (x +: acc, n)
        else
          (acc, counter - 1)
      }
        ._1
        .reverse
    }
  }

  def granulation(count: Int, granulation: Int): Int = count / granulation

}
