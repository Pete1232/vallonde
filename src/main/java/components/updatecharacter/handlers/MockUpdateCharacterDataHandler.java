package components.updatecharacter.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import components.updatecharacter.repositories.MockCharacterUpdater$;

@SuppressWarnings("unused")
public class MockUpdateCharacterDataHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        UpdateCharacterDataHandlerInternal mockUpdateCharacterDataHandler = new UpdateCharacterDataHandlerInternal(MockCharacterUpdater$.MODULE$);
        return mockUpdateCharacterDataHandler.handleRequest(input, context);
    }
}
