package it.helpers

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest, FunctionCode}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClient}
import connectors.filestore.amazon.{DefaultAmazonClientFactory, DefaultAmazonConfigProvider}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._

trait TestLambdaHelpers extends BeforeAndAfterAll {
  self: Suite =>

  private lazy val deploymentPackage: ByteBuffer = {
    val fileName = "target/scala-2.12/test.zip"
    zip(fileName, Seq("target/scala-2.12/vallonde-assembly-999-SNAPSHOT.jar"))
    ByteBuffer.wrap(Files.readAllBytes(new File(fileName).toPath))
  }

  lazy val lambda: AWSLambda = AWSLambdaClient.builder()
    .withClientConfiguration(defaultAmazonClientFactory.clientSettings.withProxyPort(4574)) //TODO config
    .build()
  private lazy val defaultAmazonClientFactory = new DefaultAmazonClientFactory(defaultAmazonConfig)
  private val defaultAmazonConfig = new DefaultAmazonConfigProvider // TODO move this class or don't use?

  override def beforeAll(): Unit = {
    super.beforeAll()
    deleteAllFunctions()
    createFunction(Functions.UPDATE_CHARACTER, "components.updatecharacter.handler.DefaultUpdateCharacterDataHandler")
    createFunction(Functions.MOCK_UPDATE_CHARACTER, "components.updatecharacter.handler.MockUpdateCharacterDataHandler")
  }

  private def createFunction(functionName: String, handler: String) = {
    lambda.createFunction(
      new CreateFunctionRequest()
        .withFunctionName(functionName)
        .withCode(new FunctionCode().withZipFile(deploymentPackage))
        .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
        .withHandler(handler)
    )
  }

  private def deleteAllFunctions() = {
    lambda.listFunctions().getFunctions.asScala
      .map { function =>
        lambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(function.getFunctionName))
      }
  }

  override def afterAll(): Unit = {
    deleteAllFunctions()
    super.afterAll()
  }

  private def zip(out: String, files: Iterable[String]): Unit = {
    import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}
    import java.util.zip.{ZipEntry, ZipOutputStream}

    val zip = new ZipOutputStream(new FileOutputStream(out))

    files.foreach { name =>
      zip.putNextEntry(new ZipEntry(name.split('/').last))
      val in = new BufferedInputStream(new FileInputStream(name))
      var b: Int = in.read()
      while (b > -1) {
        zip.write(b)
        b = in.read()
      }
      in.close()
      zip.closeEntry()
    }
    zip.close()
  }

  object Functions {
    val UPDATE_CHARACTER = "UpdateCharacter"
    val MOCK_UPDATE_CHARACTER = "MockUpdateCharacter"
  }
}
