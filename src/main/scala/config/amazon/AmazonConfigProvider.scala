package config.amazon

import com.amazonaws.{ClientConfiguration, Protocol}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

trait AmazonConfigProvider {
  def proxy: Option[(String, Int)]

  def defaultRegion: String

  def isPathStyleAccess: Option[Boolean]

  lazy val clientSettings: ClientConfiguration = {
    val base = new ClientConfiguration()

    if (proxy.isDefined) {
      base
        .withProxyHost(proxy.get._1)
        .withProxyPort(proxy.get._2)
        .withProtocol(Protocol.HTTP)
    } else base
  }
}

trait TypesafeAmazonConfigProvider extends AmazonConfigProvider {

  val configRoot: String

  lazy val config: Config = ConfigFactory.load()
  lazy val proxy: Option[(String, Int)] = Try((config.getString(s"$configRoot.proxy.host"), config.getInt(s"$configRoot.proxy.port"))).toOption
  lazy val defaultRegion: String = Try(config.getString(s"$configRoot.aws.default-region")).toOption.getOrElse("eu-west-2")
  lazy val isPathStyleAccess: Option[Boolean] = Try(config.getBoolean(s"$configRoot.path-style-access")).toOption
}

object DefaultS3ConfigProvider extends TypesafeAmazonConfigProvider {
  val configRoot: String = "akka.stream.alpakka.s3"
}
