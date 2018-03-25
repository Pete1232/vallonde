package components.upload_serverless_website.models

import io.circe.Json

case class FailureDetails(message: Json, `type`: String, externalExecutionId: String)
