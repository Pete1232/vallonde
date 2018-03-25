package components.upload_serverless_website.handlers

import com.amazonaws.services.lambda.runtime.Context
import components.upload_serverless_website.connectors.CodePipelineConnector
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, MustMatchers, OneInstancePerTest}

class UploadServerlessHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory with OneInstancePerTest {
  val logger: Logger = LogManager.getLogger(this.getClass)

  val mockCodePipeline: CodePipelineConnector = mock[CodePipelineConnector]
  val mockContext: Context = mock[Context]

  val handler = new UploadServerlessHandler(mockCodePipeline)

  val testRequest: String = scala.io.Source.fromFile("src/test/resources/code_pipeline_invoke.json").mkString

  "Calling the handler" when {
    "the input json is a CloudFormation invocation request" must {
      "return the sent json" in {
        mockCodePipeline.sendSuccessEvent _ expects(*, *)

        handler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
      "send a success event" in {
        mockCodePipeline.sendSuccessEvent _ expects("11111111-abcd-1111-abcd-111111abcdef", *) once()

        handler.handleRequest(testRequest, mockContext) mustBe testRequest
      }
    }
    "the input cannot be parsed" must {
      "return an error message" in {
        handler.handleRequest("not { validJson;", mockContext) must include("Error parsing JSON")
      }
    }
  }
}
