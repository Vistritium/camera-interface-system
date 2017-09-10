package camerainterfacesystem.azure

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import camerainterfacesystem.Config
import com.microsoft.azure.storage.CloudStorageAccount
import org.apache.commons.io.IOUtils


object Azure {

  private val config = Config().getConfig("azure")
  private val connectionString = config.getString("blobConnectionString")
  private val containerName = config.getString("container")

  private val account = CloudStorageAccount.parse(connectionString)
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

  def init() {}

}
