package connectors.filestore.amazon

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

trait S3ClientFactory {
  val s3Client: AmazonS3
}

object S3ClientFactory extends S3ClientFactory {

  import config.amazon.DefaultS3ConfigProvider._

  override lazy val s3Client: AmazonS3 = {
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
