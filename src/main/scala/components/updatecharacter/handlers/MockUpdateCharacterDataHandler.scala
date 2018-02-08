package components.updatecharacter.handlers

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.updatecharacter.repositories.MockCharacterUpdater

class MockUpdateCharacterDataHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    new UpdateCharacterDataHandler(MockCharacterUpdater)
      .handleRequest(input, context)
  }
}
