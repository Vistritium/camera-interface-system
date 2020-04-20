package camerainterfacesystem.services

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Collections

import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import javax.imageio.ImageIO
import net.coobird.thumbnailator.Thumbnails

import scala.util.Using

@Singleton
class ThumbnailMaker @Inject()(
) extends LazyLogging {

  def make(jpeg: Array[Byte]): Array[Byte] = {
    Using(new ByteArrayInputStream(jpeg)) { stream =>
      val img = Thumbnails
        .fromInputStreams(Collections.singleton(stream))
        .outputFormat("jpg")
        .width(125)
        .asBufferedImage()
      val out = new ByteArrayOutputStream()
      ImageIO.write(img, "jpg", out)
      out.toByteArray
    }.get
  }

}

