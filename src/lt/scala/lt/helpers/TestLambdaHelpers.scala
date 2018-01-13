package lt.helpers

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest, FunctionCode}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClient}
import config.amazon.TypesafeAmazonConfigProvider
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._

trait TestLambdaHelpers extends BeforeAndAfterAll {
  self: Suite =>

  lazy val lambda: AWSLambda = AWSLambdaClient.builder()
    .withClientConfiguration(defaultAmazonClientFactory.clientSettings)
    .build()

  private lazy val deploymentPackage: ByteBuffer = {
    val fileName = "target/scala-2.12/test.zip"
    zip(fileName, Seq("target/scala-2.12/vallonde-assembly-999-SNAPSHOT.jar"))
    ByteBuffer.wrap(Files.readAllBytes(new File(fileName).toPath))
  }
  private lazy val defaultAmazonClientFactory = DefaultLambdaConfigProvider

  object DefaultLambdaConfigProvider extends TypesafeAmazonConfigProvider {
    override val configRoot: String = "aws.lambda"
  }

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
