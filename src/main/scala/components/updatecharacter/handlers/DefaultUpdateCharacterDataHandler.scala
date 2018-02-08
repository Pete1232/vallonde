package components.updatecharacter.handlers

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import repositories.character.CharacterRepository

class DefaultUpdateCharacterDataHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    new UpdateCharacterDataHandler(new CharacterRepository()(scala.concurrent.ExecutionContext.global))
      .handleRequest(input, context)
  }
}
