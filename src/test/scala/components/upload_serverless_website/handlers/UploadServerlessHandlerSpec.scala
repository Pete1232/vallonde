package components.upload_serverless_website.handlers

import java.io.File
import java.nio.file.Path

import com.amazonaws.services.lambda.runtime.Context
import components.upload_serverless_website.connectors.CodePipelineConnector
import config.global.GlobalConfig
import connectors.filestore.{AwsFileLocation, FileDownloadResult, FileDownloader}
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, MustMatchers, OneInstancePerTest}

import scala.concurrent.Future

class UploadServerlessHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory with OneInstancePerTest {
  val logger: Logger = LogManager.getLogger(this.getClass)

  val mockCodePipeline: CodePipelineConnector = mock[CodePipelineConnector]
  val mockFileDownloader: FileDownloader[AwsFileLocation] = mock[FileDownloader[AwsFileLocation]]
  val mockContext: Context = mock[Context]

  val mockHandler = new UploadServerlessHandler(mockCodePipeline, mockFileDownloader, GlobalConfig)(scala.concurrent.ExecutionContext.global)

  val testRequest: String = scala.io.Source.fromFile("src/test/resources/code_pipeline_invoke.json").mkString
  val testBucket = "codepipeline-eu-west-2-846592608297"
  val testSourceLocation = "Vallonde/MyApp/SourceOutput.zip"
  val testCFLocation = "Vallonde/StackOutpu/CloudFormationOutput"
  val testJobId = "11111111-abcd-1111-abcd-111111abcdef"
  val tmp: Path = new File("/tmp").toPath

  "Calling the handler" when {
    "the input json is a CloudFormation invocation request" must {
      "return the sent json" in {
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testSourceLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testCFLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()

        mockCodePipeline.sendSuccessEvent _ expects(testJobId, *) once()

        mockHandler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
      "download all files specified in the request to the /tmp directory" in {
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testSourceLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testCFLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()

        mockCodePipeline.sendSuccessEvent _ expects(testJobId, *) once()

        mockHandler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
      "send a success event" in {
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testSourceLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()
        (mockFileDownloader.downloadFromStore _)
          .expects(AwsFileLocation(testBucket, testCFLocation), tmp)
          .returning(Future.successful(FileDownloadResult(true)))
          .once()

        mockCodePipeline.sendSuccessEvent _ expects(testJobId, *) once()

        mockHandler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
    }
    "the input cannot be parsed" must {
      "return an error message" in {
        mockHandler.handleRequest("not { validJson;", mockContext) must include("Error parsing JSON")
      }
    }
  }
}
