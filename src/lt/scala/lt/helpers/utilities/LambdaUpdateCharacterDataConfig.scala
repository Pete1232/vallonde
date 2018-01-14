package lt.helpers.utilities

import java.io.{File, FileInputStream, InputStreamReader}
import java.nio.ByteBuffer
import java.nio.file.Files

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, FunctionCode}
import io.circe.generic.auto._
import io.circe.yaml

case class LambdaUpdateCharacterDataConfig(FunctionName: String,
                                           Handler: String,
                                           Runtime: String,
                                           CodeUri: String) {
  def asCreateFunctionRequest(createMockedVersion: Boolean): CreateFunctionRequest = {
    val functionNameOverride: String = if (createMockedVersion) s"Mock$FunctionName" else FunctionName
    val handlerOverride: String = if (createMockedVersion) Handler.replaceFirst("Default", "Mock") else Handler

    new CreateFunctionRequest()
      .withFunctionName(functionNameOverride)
      .withCode(new FunctionCode().withZipFile(buildDeploymentPackage(CodeUri)))
      .withRuntime(Runtime)
      .withHandler(handlerOverride)
  }

  private def buildDeploymentPackage(codeUri: String): ByteBuffer = {
    val fileName = "target/scala-2.12/test.zip"
    zip(fileName, Seq(codeUri))
    ByteBuffer.wrap(Files.readAllBytes(new File(fileName).toPath))
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
}

object LambdaUpdateCharacterDataConfig {
  def fromCloudFormationTemplate: LambdaUpdateCharacterDataConfig = {
    val template = new FileInputStream(new File("template.yaml"))

    yaml.parser.parse(new InputStreamReader(template))
      .flatMap { template =>
        template.hcursor.downField("Resources").downField("UpdateCharacterDataFunction").downField("Properties").as[LambdaUpdateCharacterDataConfig]
      }.right.get
  }
}
