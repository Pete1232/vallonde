package components.updatecharacter.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import components.updatecharacter.repositories.CharacterUpdater
import io.circe._
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}
import repositories.character.models.CharacterModel

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UpdateCharacterDataHandler(characterUpdater: CharacterUpdater) {

  val logger: Logger = LogManager.getLogger(this.getClass)

  def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    logger.debug(s"Received update request $input")

    val inputString: String = input.getBody

    val asyncResult: Future[Option[(String, Int)]] = parser.parse(inputString)
      .flatMap { parsedJson =>
        logger.debug(s"Parsed request as $parsedJson")
        parsedJson.as[CharacterModel]
      }
      .fold(handleInputError, handleInputSuccess)

    val unsafeSyncResult: (String, Int) = Await.result(asyncResult, Duration.Inf).getOrElse(inputString -> 200)

    logger.debug(s"Returning $unsafeSyncResult")

    new APIGatewayProxyResponseEvent()
      .withStatusCode(unsafeSyncResult._2)
      .withBody(unsafeSyncResult._1)
      .withHeaders(Map("Access-Control-Allow-Origin" -> "*").asJava)
  }

  private def handleInputError(error: Error): Future[Option[(String, Int)]] = {
    Future.successful(Some(error match {
      case p: ParsingFailure => s"ParsingFailure: ${p.message}" -> 400
      case d: DecodingFailure => s"DecodingFailure: ${d.message}" -> 400
      case e => s"${e.getClass.getSimpleName}: ${e.getMessage}" -> 503
    }))
  }

  private def handleInputSuccess(input: CharacterModel): Future[Option[(String, Int)]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    characterUpdater.updateRecordByName(input.name, input)
      .map(_.errors)
      .recover {
        case e: Throwable => Some(e.getMessage)
      }
      .map(_.map(_ -> 503))
  }
}
