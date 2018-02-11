import sbt._

object ProjectConfigs {
  lazy val LambdaTest = config("lt") extend Test
  lazy val LambdaTestClean = config("lt-clean") extend LambdaTest
}
