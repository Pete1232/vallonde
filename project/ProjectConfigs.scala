import sbt._

object ProjectConfigs {
  lazy val LambdaTest = config("lt") extend Test
}
