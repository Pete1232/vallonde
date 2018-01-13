package connectors.filestore.amazon

import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import config.amazon.{AmazonClientFactory, DefaultAmazonClientFactory, DefaultS3ConfigProvider}

trait S3ClientFactory extends AmazonClientFactory[AmazonS3] {
  val client: AmazonS3
}

object S3ClientFactory
  extends DefaultAmazonClientFactory[AmazonS3ClientBuilder, AmazonS3](DefaultS3ConfigProvider)
    with S3ClientFactory {

  override lazy val defaultClient: AwsSyncClientBuilder[AmazonS3ClientBuilder, AmazonS3] = {
    val base: AmazonS3ClientBuilder = AmazonS3ClientBuilder.standard()

    amazonConfigProvider.isPathStyleAccess match {
      case Some(true) => base.enablePathStyleAccess()
      case _ => base
    }
  }
}
