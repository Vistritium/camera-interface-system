package camerainterfacesystem.web.controllers.upload

import java.io.{InputStream, OutputStream, PipedInputStream}

import camerainterfacesystem.Config
import com.microsoft.azure.storage.CloudStorageAccount
import org.apache.commons.io.IOUtils


object AzureUploader {

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


}
