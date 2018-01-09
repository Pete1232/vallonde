package components.updatecharacter.handler

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.updatecharacter.repositories.CharacterUpdater
import repositories.character.models.{CharacterModel, UpdateCharacterDataResponse}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UpdateCharacterDataHandler(characterUpdater: CharacterUpdater) extends RequestHandler[CharacterModel, UpdateCharacterDataResponse] {
  override def handleRequest(input: CharacterModel, context: Context): UpdateCharacterDataResponse = {

    Await.result(
      characterUpdater.updateRecordByName(input.name, input)
      , Duration.Inf)
  }
}
