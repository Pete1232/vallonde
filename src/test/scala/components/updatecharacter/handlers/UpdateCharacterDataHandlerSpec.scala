package components.updatecharacter.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import components.updatecharacter.repositories.CharacterUpdater
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, MustMatchers}
import repositories.character.models.{CharacterModel, StatsModel, UpdateCharacterDataResponse}

import scala.concurrent.Future

class UpdateCharacterDataHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory {

  val updater: CharacterUpdater = mock[CharacterUpdater]
  val mockContext: Context = mock[Context]

  val handler = new UpdateCharacterDataHandler(updater)

  "Calling the handle request method" must {
    "return 200 status if no error was thrown" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(None))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getStatusCode mustBe 200
    }

    "return the input value if no error was thrown" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(None))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getBody mustBe inputAsJsonString
    }

    "return 400 status if the input could not be parsed as json" in {
      val input: String = "notJson"
      val requestWithInvalidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(input)

      updater.updateRecordByName _ expects(*, *) never()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithInvalidBody, mockContext)

      actualResponse.getStatusCode mustBe 400
    }

    "return an error if the input could not be parsed as json" in {
      val input: String = "notJson"
      val requestWithInvalidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(input)

      updater.updateRecordByName _ expects(*, *) never()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithInvalidBody, mockContext)

      actualResponse.getBody must startWith("ParsingFailure")
    }

    "return 400 status if the input could not be decoded as a Character" in {
      val input: String = "{\"isJson\":\"true\"}"
      val requestWithInvalidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(input)

      updater.updateRecordByName _ expects(*, *) never()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithInvalidBody, mockContext)

      actualResponse.getStatusCode mustBe 400
    }

    "return an error if the input could not be decoded as a Character" in {
      val input: String = "{\"isJson\":\"true\"}"
      val requestWithInvalidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(input)

      updater.updateRecordByName _ expects(*, *) never()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithInvalidBody, mockContext)

      actualResponse.getBody must startWith("DecodingFailure")
    }

    "return 503 status if an error was received from the database layer" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(Some(errorMessage)))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getStatusCode mustBe 503
    }

    "return an error message received from the database layer" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(Some(errorMessage)))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getBody mustBe errorMessage
    }

    "return 503 status if the database layer threw an error" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))

      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.failed(new Exception(errorMessage))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getStatusCode mustBe 503
    }

    "return an error if the database layer threw an error" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))

      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.failed(new Exception(errorMessage))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getBody mustBe errorMessage
    }

    "contain an access control header allowing all origins" in {

      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputAsJsonString: String = input.asJson.toString
      val requestWithValidBody: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withBody(inputAsJsonString)

      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(None))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(requestWithValidBody, mockContext)

      actualResponse.getHeaders.get("Access-Control-Allow-Origin") mustBe "*"
    }
  }
}
