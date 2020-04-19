package camerainterfacesystem.configuration

import camerainterfacesystem.web.controllers.upload.UploadReceiverActorFactory
import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

@Configuration
class FactoriesModule extends ScalaModule {
  override def configure(): Unit = {
    install(new FactoryModuleBuilder().build(classOf[UploadReceiverActorFactory]))
  }
}
