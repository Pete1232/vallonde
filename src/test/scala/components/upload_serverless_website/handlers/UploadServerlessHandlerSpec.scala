package components.upload_serverless_website.handlers

import java.io.File
import java.nio.file.Path

import com.amazonaws.SdkClientException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.model.{GroupGrantee, Permission}
import components.upload_serverless_website.connectors.CodePipelineConnector
import components.upload_serverless_website.models.FailureEventDetails
import config.global.GlobalConfig
import connectors.filestore._
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, MustMatchers, OneInstancePerTest}

import scala.concurrent.Future

class UploadServerlessHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory with OneInstancePerTest {
  val logger: Logger = LogManager.getLogger(this.getClass)

  val mockCodePipeline: CodePipelineConnector = mock[CodePipelineConnector]
  val mockFileDownloader: FileDownloader[AwsFileLocation] = mock[FileDownloader[AwsFileLocation]]
  val mockFileUploader: FileUploader[AwsFileLocation] = mock[FileUploader[AwsFileLocation]]
  val mockContext: Context = mock[Context]

  val mockHandler = new UploadServerlessHandler(
    mockCodePipeline,
    mockFileDownloader,
    mockFileUploader,
    GlobalConfig)(scala.concurrent.ExecutionContext.global)

  val testResourcesDirectory = "src/test/resources"
  val testRequest: String = scala.io.Source.fromFile(s"$testResourcesDirectory/code_pipeline_invoke.json").mkString
  val testBucket = "codepipeline-eu-west-2-846592608297"
  val testSourceLocation = "Vallonde/MyApp/SourceOutput.zip"
  val testCFLocation = "Vallonde/StackOutpu/CloudFormationOutput.zip"
  val testJobId = "11111111-abcd-1111-abcd-111111abcdef"
  val tmpSourceLocation: Path = new File(s"/tmp/$testSourceLocation").toPath
  val tmpCFLocation: Path = new File(s"/tmp/$testCFLocation").toPath
  val realSourceLocation: Path = new File(s"$testResourcesDirectory/$testSourceLocation").toPath
  val realCFLocation: Path = new File(s"$testResourcesDirectory/$testCFLocation").toPath
  val extractedSourceLocation = "/tmp"
  val extractedCFLocation = "/tmp/vallonde-dev-stack-output.json"

  private val setHappyPathExpectations = () => {
    (mockFileDownloader.downloadFromStore _)
      .expects(AwsFileLocation(testBucket, testSourceLocation), tmpSourceLocation)
      .returning(Future.successful(realSourceLocation))
      .once()
    (mockFileDownloader.downloadFromStore _)
      .expects(AwsFileLocation(testBucket, testCFLocation), tmpCFLocation)
      .returning(Future.successful(realCFLocation))
      .once()
    (mockFileUploader.pushToStore _)
      .expects(
        new File(s"$extractedSourceLocation/src/main/public/html/character.html").toPath,
        AwsFileLocation("vallonde", "src/main/public/html/character.html",
          Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read))))
      .returning(Future.successful(FileUploadResult(FileStore.S3, Map.empty)))
    (mockFileUploader.pushToStore _)
      .expects(
        new File("/tmp/config.js").toPath,
        AwsFileLocation("vallonde", "src/main/assets/js/config.js",
          Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read))))
      .returning(Future.successful(FileUploadResult(FileStore.S3, Map.empty)))
    (mockFileUploader.pushToStore _)
      .expects(
        new File(s"$extractedSourceLocation/src/main/assets/js/update_character.js").toPath,
        AwsFileLocation("vallonde-assets", "src/main/assets/js/update_character.js"))
      .returning(Future.successful(FileUploadResult(FileStore.S3, Map.empty)))
    mockCodePipeline.sendSuccessEvent _ expects(testJobId, *) once()
  }

  "Calling the handler" when {
    "the input json is a CloudFormation invocation request" must {
      "download all files in the request and send a success event" in {
        setHappyPathExpectations()

        mockHandler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
      "unzip the downloaded files in the /tmp directory" in {
        setHappyPathExpectations()

        mockHandler.handleRequest(testRequest, mockContext)

        val outputFiles: Boolean = Seq(
          new File(extractedSourceLocation),
          new File(extractedCFLocation)
        ).map { file => file.deleteOnExit(); file.exists() }
          .reduce(_ && _)

        outputFiles mustBe true
      }
    }
    "the input cannot be parsed" must {
      "return an error message" in {
        mockHandler.handleRequest("not { validJson;", mockContext) must include("Error parsing JSON")
      }
    }
    "the downloaded file could not be decompressed" must {
      "send a failure event with the error message" in {

        val errorMessage = "Error extracting file"

        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testSourceLocation), tmpSourceLocation)
          .returning(Future.successful(tmpSourceLocation))
          .once()
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testCFLocation), tmpCFLocation)
          .returning(Future.successful(tmpCFLocation))
          .once()
        mockCodePipeline.sendFailureEvent _ expects(testJobId, FailureEventDetails(errorMessage)) once()

        mockHandler.handleRequest(testRequest, mockContext) must include(errorMessage)
      }
    }
    "the file is not downloaded to the expected location" must {
      "send a failure event with an error message" in {
        val errorMessage = "Runtime error when uploading files"

        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testSourceLocation), tmpSourceLocation)
          .returning(Future.successful(realSourceLocation))
          .once()
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testCFLocation), tmpCFLocation)
          .returning(Future.successful(realCFLocation))
          .once()
        (mockFileUploader.pushToStore _)
          .expects(
            new File(s"$extractedSourceLocation/src/main/public/html/character.html").toPath,
            AwsFileLocation("vallonde", "src/main/public/html/character.html",
              Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read))))
          .returning(Future.failed(new SdkClientException(
            """ Unable to calculate MD5 hash: \tmp\SourceOutput\src\main\public\html\character.html (The system cannot find the path specified)"""
          )))
        (mockFileUploader.pushToStore _)
          .expects(
            new File("/tmp/config.js").toPath,
            AwsFileLocation("vallonde", "src/main/assets/js/config.js",
              Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read))))
          .returning(Future.successful(FileUploadResult(FileStore.S3, Map.empty)))
        (mockFileUploader.pushToStore _)
          .expects(
            new File(s"$extractedSourceLocation/src/main/assets/js/update_character.js").toPath,
            AwsFileLocation("vallonde-assets", "src/main/assets/js/update_character.js"))
          .returning(Future.successful(FileUploadResult(FileStore.S3, Map.empty)))
        mockCodePipeline.sendFailureEvent _ expects(testJobId, FailureEventDetails(errorMessage)) once()

        mockHandler.handleRequest(testRequest, mockContext) must include(errorMessage)
      }
    }
  }

  "The config builder method" must {
    "create a js config file with the api base url in the input file" in {
      UploadServerlessHandler.buildConfigFile(
        new File(s"$testResourcesDirectory/Vallonde/StackOutpu/vallonde-dev-stack-output.json"))

      val result = new File("/tmp/config.js")

      scala.io.Source.fromFile(result).mkString must include(
        """"api-base-url" : "https://i04f64fzsi.execute-api.eu-west-2.amazonaws.com"""".stripMargin
      )
    }
    "create a js config file with the correct s3 bucket url" in {
      UploadServerlessHandler.buildConfigFile(
        new File(s"$testResourcesDirectory/Vallonde/StackOutpu/vallonde-dev-stack-output.json"))

      val result = new File("/tmp/config.js")

      scala.io.Source.fromFile(result).mkString must include(
        """"assets-uri" : "https://s3.eu-west-2.amazonaws.com/vallonde-assets/src/main/assets/js/update_character.js""".stripMargin
      )
    }
  }
}
