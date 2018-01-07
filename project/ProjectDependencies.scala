import sbt._

object ProjectDependencies {

  private val aws_sdk = Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "0.15.1",
    "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.15.1"
  )

  private val compile = aws_sdk ++ Seq(
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
    "io.circe" %% "circe-generic" % "0.9.0"
  )).map(_ % IntegrationTest)

  def apply(): Seq[ModuleID] = compile ++ test ++ it
}
