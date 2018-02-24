package components.get_character.handlers

import cats.data.EitherT
import cats.implicits._
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import components.get_character.repositories.CharacterGetter
import config.global.GlobalConfig
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalacheck.Gen
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.exceptions.TestFailedException
import org.scalatest.{AsyncWordSpec, MustMatchers, OneInstancePerTest}
import repositories.character.models.{CharacterModel, StatsModel}
import test.helpers.PropertyTesting

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class GetCharacterDataHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory
  with PropertyTesting with OneInstancePerTest {

  val logger: Logger = LogManager.getLogger(this.getClass)

  val getter: CharacterGetter = mock[CharacterGetter]
  val mockContext: Context = mock[Context]

  val handler = new GetCharacterDataHandler(getter, GlobalConfig)(scala.concurrent.ExecutionContext.global)

  "Calling the handle request method" must {
    "return 200 status if the character model was found" in {

      forAll(Gen.alphaNumStr suchThat (_.length > 0)) { name: String =>

        val responseModel: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
        val responseAsFuture: Future[Either[String, CharacterModel]] = Future.successful(Either.right(responseModel))
        val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
          .withPath(s"character/$name")

        val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
        logger.debug(s"Prepared db response ${dbResponse.toString}")
        getter.getRecordByName _ expects name returning dbResponse once

        logger.debug("Calling handle request function")
        val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

        actualResponse.getStatusCode mustBe 200
      }
    }
    "return a response containing the character model if found" in {

      forAll(Gen.alphaNumStr suchThat (_.length > 0)) { name: String =>

        val responseModel: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
        val responseAsFuture: Future[Either[String, CharacterModel]] = Future.successful(Either.right(responseModel))
        val responseModelAsJson: String = responseModel.asJson.noSpaces
        val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
          .withPath(s"character/$name")

        val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
        logger.debug(s"Prepared db response ${dbResponse.toString}")
        getter.getRecordByName _ expects name returning dbResponse once

        logger.debug("Calling handle request function")
        val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

        actualResponse.getBody mustBe responseModelAsJson
      }
    }
    "return 503 status if an error is received from the database layer" in {
      val name = "bob"
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      val errorMessage = "Something went wrong"
      val responseAsFuture: Future[Either[String, CharacterModel]] = Future.successful(Either.left(errorMessage))

      val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
      getter.getRecordByName _ expects name returning dbResponse once

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getStatusCode mustBe 503
    }
    "return an error message received from the database layer" in {
      val name = "bob"
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      val errorMessage = "Something went wrong"
      val responseAsFuture: Future[Either[String, CharacterModel]] = Future.successful(Either.left(errorMessage))

      val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
      getter.getRecordByName _ expects name returning dbResponse once

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getBody mustBe errorMessage
    }
    "return 503 status if the database layer threw an error" in {
      val name = "bob"
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      val errorMessage = "Something went wrong"
      val responseAsFuture: Future[Either[String, CharacterModel]] = Future.failed(new Exception(errorMessage))

      val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
      getter.getRecordByName _ expects name returning dbResponse once

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getStatusCode mustBe 503
    }
    "return an error if the database layer threw an error" in {
      val name = "bob"
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      val errorMessage = "Something went wrong"
      val responseAsFuture: Future[Either[String, CharacterModel]] = Future.failed(new Exception(errorMessage))

      val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)
      getter.getRecordByName _ expects name returning dbResponse once

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getBody mustBe errorMessage
    }
    "return an error message if the Future times out" in {
      val name = "bob"
      val timeoutConfig: GlobalConfig = new GlobalConfig {
        override def futureTimeout: FiniteDuration = 1 nanosecond
      }
      val timeoutHandler: GetCharacterDataHandler = {
        new GetCharacterDataHandler(getter, timeoutConfig)(scala.concurrent.ExecutionContext.global)
      }

      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      lazy val responseAsFuture: Future[Either[String, CharacterModel]] = {
        Future {
          Thread.sleep(10) // to make sure the time to complete the future is > 1 ns
          throw new TestFailedException("Future should have timed out", 0)
        }
      }

      val errorMessage = "Futures timed out"

      getter.getRecordByName _ expects * returning EitherT(responseAsFuture) noMoreThanOnce

      val actualResponse: APIGatewayProxyResponseEvent = timeoutHandler.handleRequest(request, mockContext)

      actualResponse.getBody mustBe errorMessage
    }
    "return a 400 status if no character name was provided" in {
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()

      getter.getRecordByName _ expects * never

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getStatusCode mustBe 400
    }
    "return an error message if no character name was provided" in {
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()

      getter.getRecordByName _ expects * never

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getBody mustBe "Character name must be provided"
    }
    "contain an access control header allowing all origins" in {
      val name = "test"
      val request: APIGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent()
        .withPath(s"character/$name")

      val responseModel: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val responseAsFuture: Future[Either[String, CharacterModel]] = Future.successful(Either.right(responseModel))
      val dbResponse: EitherT[Future, String, CharacterModel] = EitherT.apply(responseAsFuture)

      logger.debug("Calling handle request function")
      getter.getRecordByName _ expects name returning dbResponse anyNumberOfTimes()

      val actualResponse: APIGatewayProxyResponseEvent = handler.handleRequest(request, mockContext)

      actualResponse.getHeaders.get("Access-Control-Allow-Origin") mustBe "*"
    }
  }
}
