package components.upload_serverless_website.connectors

import components.upload_serverless_website.models.{FailureDetails, SuccessDetails}

trait CodePipelineConnector {

  def sendSuccessEvent(jobId: String, successDetails: SuccessDetails): Unit

  def sendFailureEvent(jobId: String, failureDetails: FailureDetails): Unit
}
