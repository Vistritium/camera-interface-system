package camerainterfacesystem.azure

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import camerainterfacesystem.configuration.ConfigLoader
import com.microsoft.azure.storage.CloudStorageAccount
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

@Singleton
class Azure @Inject()(
  mainConfig: Config
) extends LazyLogging {

  private val config = mainConfig.getConfig("azure")
  private val connectionString = config.getString("blobConnectionString")
  private val containerName = config.getString("container")

  private val account = try {
    CloudStorageAccount.parse(connectionString)
  } catch {
    case e: Exception => throw new IllegalStateException(s"Wrong connection string: ${connectionString}", e)
  }
  private val serviceClient = account.createCloudBlobClient()
  private val container = serviceClient.getContainerReference(containerName)
  require(container.exists(), s"azure container must exist: $containerName")

  def upload(path: String, inputStream: InputStream, length: Int): Unit = {
    val blob = container.getBlockBlobReference(path)
    blob.upload(inputStream, length)
  }

  def download(path: String, outputStream: OutputStream): Unit = {
    val blob = container.getBlobReferenceFromServer(path)
    blob.download(outputStream)
  }

  def download(path: String): Array[Byte] = {
    val blob = container.getBlobReferenceFromServer(path)
    val outputStream = new ByteArrayOutputStream(50000)
    blob.download(outputStream)
    outputStream.toByteArray
  }

  def deleteBlob(path: String): Unit = {
    val blob = container.getAppendBlobReference(path)
    blob.delete()
  }

  def init() {}

}
