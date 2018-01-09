package components.updatecharacter.handler

import com.amazonaws.services.lambda.runtime.Context
import components.updatecharacter.repositories.CharacterUpdater
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, MustMatchers}
import repositories.character.models.{CharacterModel, StatsModel, UpdateCharacterDataResponse}

import scala.concurrent.Future

class UpdateCharacterHandlerSpec extends AsyncWordSpec with MustMatchers with AsyncMockFactory {

  val updater: CharacterUpdater = mock[CharacterUpdater]

  val handler: UpdateCharacterDataHandler = new UpdateCharacterDataHandler(updater)

  "Calling the handle method on the update character data handler" must {
    "correctly parse the input and pass it to the character updater" in {

      val model = CharacterModel("testname", 1, StatsModel(1, 1, 1, 1, 1, 1))

      val mockContext: Context = mock[Context]

      updater.updateRecordByName _ expects(model.name, model) returning Future.successful(UpdateCharacterDataResponse(None)) once()

      handler.handleRequest(model, mockContext) mustBe UpdateCharacterDataResponse(None)
    }
  }
}
