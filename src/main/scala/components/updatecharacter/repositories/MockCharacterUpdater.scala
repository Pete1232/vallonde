package components.updatecharacter.repositories
import repositories.character.models.{CharacterModel, UpdateCharacterDataResponse}

import scala.concurrent.Future

object MockCharacterUpdater extends CharacterUpdater {
  override def updateRecordByName(name: String, character: CharacterModel): Future[UpdateCharacterDataResponse] = {
    Future.successful(UpdateCharacterDataResponse(None))
  }
}
