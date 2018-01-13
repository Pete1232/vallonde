package config.amazon

import com.amazonaws.client.builder.AwsSyncClientBuilder

trait AmazonClientFactory[TypeToBuild] {
  val client: TypeToBuild
}

abstract class DefaultAmazonClientFactory[S <: AwsSyncClientBuilder[S, T], T](val amazonConfigProvider: AmazonConfigProvider)
  extends AmazonClientFactory[T] {

  override lazy val client: T = {

    import amazonConfigProvider._

    defaultClient
      .withClientConfiguration(clientSettings)
      .withRegion(defaultRegion)
      .build()
  }
  protected val defaultClient: AwsSyncClientBuilder[S, T]
}
