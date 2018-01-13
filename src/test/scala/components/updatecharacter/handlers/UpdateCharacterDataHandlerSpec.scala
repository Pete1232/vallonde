package components.updatecharacter.handlers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.amazonaws.services.lambda.runtime.Context
import components.updatecharacter.handler.UpdateCharacterDataHandler
import components.updatecharacter.repositories.CharacterUpdater
import io.circe.generic.auto._
import io.circe.parser._
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
    "return the input value if no error was thrown" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputStream: ByteArrayInputStream = new ByteArrayInputStream(input.asJson.toString.getBytes())
      val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(None))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      handler.handleRequest(inputStream, outputStream, mockContext)

      parse(new String(outputStream.toByteArray)).flatMap(_.as[CharacterModel]).right.get mustBe input
    }

    "return an error if the input could not be parsed as json" in {
      val input: String = "notJson"
      val inputStream: ByteArrayInputStream = new ByteArrayInputStream(input.getBytes())
      val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      updater.updateRecordByName _ expects(*, *) never()

      handler.handleRequest(inputStream, outputStream, mockContext)

      new String(outputStream.toByteArray) must startWith("ParsingFailure")
    }

    "return an error if the input could not be decoded as a Character" in {
      val input: String = "{\"isJson\":\"true\"}"
      val inputStream: ByteArrayInputStream = new ByteArrayInputStream(input.getBytes())
      val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      updater.updateRecordByName _ expects(*, *) never()

      handler.handleRequest(inputStream, outputStream, mockContext)

      new String(outputStream.toByteArray) must startWith("DecodingFailure")
    }

    "return an error message received from the database layer" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputStream: ByteArrayInputStream = new ByteArrayInputStream(input.asJson.toString.getBytes())
      val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.successful(UpdateCharacterDataResponse(Some(errorMessage)))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      handler.handleRequest(inputStream, outputStream, mockContext)

      new String(outputStream.toByteArray) mustBe errorMessage
    }
    "return an error if the database layer threw an error" in {
      val name = "bob"
      val input: CharacterModel = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))
      val inputStream: ByteArrayInputStream = new ByteArrayInputStream(input.asJson.toString.getBytes())
      val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      val errorMessage = "Something went wrong"
      val dbResponse: Future[UpdateCharacterDataResponse] = Future.failed(new Exception(errorMessage))
      updater.updateRecordByName _ expects(name, input) returning dbResponse once()

      handler.handleRequest(inputStream, outputStream, mockContext)

      new String(outputStream.toByteArray) mustBe errorMessage
    }
  }
}
