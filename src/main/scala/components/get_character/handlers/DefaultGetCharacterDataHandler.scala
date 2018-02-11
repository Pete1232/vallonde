package components.get_character.handlers

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import config.global.GlobalConfig
import repositories.character.CharacterRepository

import scala.concurrent.ExecutionContext

class DefaultGetCharacterDataHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    new GetCharacterDataHandler(new CharacterRepository(), GlobalConfig)
      .handleRequest(input, context)
  }
}
