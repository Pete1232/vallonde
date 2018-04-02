package components.upload_serverless_website.connectors

import components.upload_serverless_website.models.{FailureEventDetails, SuccessEventDetails}

trait CodePipelineConnector {

  def sendSuccessEvent(jobId: String, successDetails: SuccessEventDetails): Unit

  def sendFailureEvent(jobId: String, failureDetails: FailureEventDetails): Unit
}
