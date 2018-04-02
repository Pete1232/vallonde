package components.upload_serverless_website.connectors

import com.amazonaws.services.codepipeline.model._
import com.amazonaws.services.codepipeline.{AWSCodePipeline, AWSCodePipelineClientBuilder}
import components.upload_serverless_website.models.{FailureEventDetails, SuccessEventDetails}

class DefaultCodePipelineConnector extends CodePipelineConnector {

  val client: AWSCodePipeline = AWSCodePipelineClientBuilder.defaultClient()

  override def sendSuccessEvent(jobId: String, successDetails: SuccessEventDetails): Unit = {
    client.putJobSuccessResult(
      new PutJobSuccessResultRequest()
        .withJobId(jobId)
        .withExecutionDetails(new ExecutionDetails()
          .withSummary(successDetails.message))
    )
  }

  override def sendFailureEvent(jobId: String, failureDetails: FailureEventDetails): Unit = {
    client.putJobFailureResult(
      new PutJobFailureResultRequest()
        .withJobId(jobId)
        .withFailureDetails(new FailureDetails()
          .withMessage(failureDetails.message)
          .withType(FailureType.JobFailed)
        )
    )
  }
}
