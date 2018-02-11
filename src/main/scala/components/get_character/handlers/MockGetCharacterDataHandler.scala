package components.get_character.handlers

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.get_character.repositories.MockCharacterGetter
import config.global.GlobalConfig

import scala.concurrent.ExecutionContext

class MockGetCharacterDataHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    new GetCharacterDataHandler(MockCharacterGetter, GlobalConfig)(ExecutionContext.global)
      .handleRequest(input, context)
  }
}
