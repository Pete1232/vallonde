package components.updatecharacter.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import components.updatecharacter.repositories.CharacterUpdater;

@SuppressWarnings("unused")
public abstract class UpdateCharacterDataHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private CharacterUpdater characterUpdater;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        UpdateCharacterDataHandlerInternal mockUpdateCharacterDataHandler = new UpdateCharacterDataHandlerInternal(characterUpdater);
        return mockUpdateCharacterDataHandler.handleRequest(input, context);
    }
}
