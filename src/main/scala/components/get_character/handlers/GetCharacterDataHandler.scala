package components.get_character.handlers

import cats.data.EitherT
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import components.get_character.models.GetCharacterResponse
import components.get_character.repositories.CharacterGetter
import config.global.GlobalConfig
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}
import scala.language.postfixOps
import scala.util.Try

class GetCharacterDataHandler(characterGetter: CharacterGetter, globalConfig: GlobalConfig)
                             (implicit ec: ExecutionContext) {

  val logger: Logger = LogManager.getLogger(this.getClass)

  def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    logger.debug(s"Received get character request $input")

    lazy val asyncResult: Future[GetCharacterResponse] = {
      (for {
        nameToQuery <- EitherT.fromEither[Future](GetCharacterDataHandler.pathToName(input.getPath))
        result <- characterGetter.getRecordByName(nameToQuery).leftMap(message => GetCharacterResponse(503, message))
      } yield result).fold(identity, result => GetCharacterResponse(200, result.asJson.noSpaces))
    }

    val unsafeSyncResult: GetCharacterResponse = {
      Try(Await.result(asyncResult, globalConfig.futureTimeout))
        .fold(_ match {
          case _: TimeoutException => GetCharacterResponse(503, "Futures timed out")
          case t: Throwable => GetCharacterResponse(503, t.getMessage)
        }, identity)
    }

    logger.debug(s"Returning $unsafeSyncResult")


    new APIGatewayProxyResponseEvent()
      .withStatusCode(unsafeSyncResult.status)
      .withBody(unsafeSyncResult.body)
  }
}

object GetCharacterDataHandler {
  def pathToName(path: String): Either[GetCharacterResponse, String] = {
    Try(path.split("character/").apply(1))
      .toEither
      .leftMap(_ => GetCharacterResponse(400, "Character name must be provided"))
  }
}
