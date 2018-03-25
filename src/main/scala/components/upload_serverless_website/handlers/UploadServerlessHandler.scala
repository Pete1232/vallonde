package components.upload_serverless_website.handlers

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.upload_serverless_website.connectors.CodePipelineConnector
import components.upload_serverless_website.models.SuccessDetails
import io.circe.HCursor
import io.circe.parser._

class UploadServerlessHandler(codePipelineConnector: CodePipelineConnector) extends RequestHandler[String, String] {
  override def handleRequest(input: String, context: Context): String = {

    parse(input)
      .flatMap { doc =>
        val cursor: HCursor = doc.hcursor
        cursor.downField("CodePipeline.job").downField("id").as[String]
      }.map { jobId =>
      codePipelineConnector.sendSuccessEvent(jobId, SuccessDetails(jobId))
    }.fold(e => s"Error parsing JSON: ${e.getMessage}", _ => input)
  }
}
