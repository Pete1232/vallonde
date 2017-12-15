package connectors.filestore.amazon

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.{ClientConfiguration, Protocol}

trait AmazonClientFactory {
  val client: AmazonS3
}

class DefaultAmazonClientFactory(amazonConfigProvider: AmazonConfigProvider) extends AmazonClientFactory {

  import amazonConfigProvider._

  lazy val clientSettings: ClientConfiguration = {
    val base = new ClientConfiguration()

    if (proxy.isDefined) {
      base
        .withProxyHost(proxy.get._1)
        .withProxyPort(proxy.get._2)
        .withProtocol(Protocol.HTTP)
    } else base
  }

  override lazy val client: AmazonS3 = {
    val base: AmazonS3ClientBuilder = AmazonS3ClientBuilder
      .standard()
      .withClientConfiguration(clientSettings)
      .withRegion(defaultRegion)

    (isPathStyleAccess match {
      case Some(true) => base.enablePathStyleAccess()
      case _ => base
    }).build()
  }
}
