import sbt._

object ProjectDependencies {

  private val aws_sdk = Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "0.15",
    "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.15"
  )

  private val compile = aws_sdk ++ Seq(
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
    "com.amazonaws" % "DynamoDBLocal" % "[1.11,2.0)",
    "io.findify" %% "s3mock" % "0.2.4"
  )).map(_ % IntegrationTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it
}
