import ProjectConfigs._
import sbt._

object ProjectDependencies {

  val alpakkaVersion = "0.15.1"
  val circeVersion = "0.9.0"

  private val aws_sdk = Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % alpakkaVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-s3" % alpakkaVersion
  )

  private val circe = {
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  }

  private val logging = Seq(
    "com.amazonaws" % "aws-lambda-java-log4j2" % "1.0.0",
    "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
    "org.apache.logging.log4j" % "log4j-api" % "2.8.2"
  )

  private val compile = aws_sdk ++ circe ++ logging ++ Seq(
    "org.scala-lang" % "scala-library" % "2.12.4",
    "org.scalactic" %% "scalactic" % "3.0.4"
  )

  private val test_common = Seq(
    "org.scalatest" %% "scalatest" % "3.0.4"
  )

  private val test = (test_common ++ Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.5",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0"
  )).map(_ % Test)

  private val it = (test_common ++ Seq(
    "io.circe" %% "circe-yaml" % "0.7.0",
    "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.226"
  )).map(_ % IntegrationTest)

  private val lt = (test_common ++ Seq(
    "io.circe" %% "circe-yaml" % "0.7.0",
    "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.226"
  )).map(_ % LambdaTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it ++ lt
}
