package config.global

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

trait GlobalConfig {
  def futureTimeout: FiniteDuration
}

object GlobalConfig extends GlobalConfig {

  lazy val config: Config = ConfigFactory.load()

  override val futureTimeout: FiniteDuration = {
    Try(config.getInt("global.futureTimeoutInMillis")).getOrElse(2000) milliseconds
  }
}
