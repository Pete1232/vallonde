package it.helpers

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest, FunctionCode}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClient}
import connectors.filestore.amazon.{DefaultAmazonClientFactory, DefaultAmazonConfigProvider}
import org.scalatest.Suite

import scala.collection.JavaConverters._

trait TestLambdaHelpers extends TestDatabaseHelpers {
  self: Suite =>

  lazy val lambda: AWSLambda = AWSLambdaClient.builder()
    .withClientConfiguration(defaultAmazonClientFactory.clientSettings.withProxyPort(4574)) //TODO config
    .build()
  private lazy val defaultAmazonClientFactory = new DefaultAmazonClientFactory(defaultAmazonConfig)
  private val defaultAmazonConfig = new DefaultAmazonConfigProvider // TODO move this class or don't use?

  override def beforeEach(): Unit = {
    super.beforeEach()

    val buffer: ByteBuffer = ByteBuffer.wrap(
      Files.readAllBytes(
        new File("target/scala-2.12/test.zip").toPath
      )
    )
    //TODO need to generate this jar dynamically and place it in a zip for localstack

    lambda.listFunctions().getFunctions.asScala
      .map { function =>
        lambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(function.getFunctionName))
      }

    // TODO from the cloudformation template
    lambda.createFunction(
      new CreateFunctionRequest()
        .withFunctionName("UpdateCharacter")
        .withCode(new FunctionCode().withZipFile(buffer))
        .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
        .withHandler("components.updatecharacter.handler.MockUpdateCharacterDataHandler")
    )
  }

  override def afterEach(): Unit = {
    lambda.listFunctions().getFunctions.asScala
      .map { function =>
        lambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(function.getFunctionName))
      }
    super.afterEach()
  }
}
