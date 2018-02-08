package components.updatecharacter.handlers

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.updatecharacter.repositories.CharacterUpdater
import io.circe._
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}
import repositories.character.models.CharacterModel

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UpdateCharacterDataHandler(characterUpdater: CharacterUpdater)
  extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  val logger: Logger = LogManager.getLogger(this.getClass)

  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    logger.debug(s"Received update request $input")

    val inputString: String = input.getBody

    val asyncResult: Future[Option[String]] = parser.parse(inputString)
      .flatMap { parsedJson =>
        logger.debug(s"Parsed request as $parsedJson")
        parsedJson.as[CharacterModel]
      }
      .fold(handleInputError, handleInputSuccess)

    val unsafeSyncResult: String = Await.result(asyncResult, Duration.Inf).getOrElse(inputString)

    logger.debug(s"Returning $unsafeSyncResult")

    new APIGatewayProxyResponseEvent()
      .withBody(unsafeSyncResult)
  }

  private def handleInputError(error: Error): Future[Option[String]] = {
    Future.successful(Some(error match {
      case p: ParsingFailure => s"ParsingFailure: ${p.message}"
      case d: DecodingFailure => s"DecodingFailure: ${d.message}"
      case e => s"${e.getClass.getSimpleName}: ${e.getMessage}"
    }))
  }

  private def handleInputSuccess(input: CharacterModel): Future[Option[String]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    characterUpdater.updateRecordByName(input.name, input)
      .map(_.errors)
      .recover {
        case e: Throwable => Some(e.getMessage)
      }
  }
}
