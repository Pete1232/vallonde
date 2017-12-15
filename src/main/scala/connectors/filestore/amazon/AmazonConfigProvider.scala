package connectors.filestore.amazon

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

trait AmazonConfigProvider {
  def proxy: Option[(String, Int)]

  def defaultRegion: String

  def isPathStyleAccess: Option[Boolean]
}

class DefaultAmazonConfigProvider extends AmazonConfigProvider {

  lazy val config: Config = ConfigFactory.load()
  lazy val proxy: Option[(String, Int)] = Try((config.getString(s"$configRoot.proxy.host"), config.getInt(s"$configRoot.proxy.port"))).toOption
  lazy val defaultRegion: String = Try(config.getString(s"$configRoot.aws.default-region")).toOption.getOrElse("eu-west-2")
  lazy val isPathStyleAccess: Option[Boolean] = Try(config.getBoolean(s"$configRoot.path-style-access")).toOption
  val configRoot: String = "akka.stream.alpakka.s3"
}
