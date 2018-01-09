package components.updatecharacter.repositories

import repositories.character.models.{CharacterModel, UpdateCharacterDataResponse}

import scala.concurrent.Future

trait CharacterUpdater {
  def updateRecordByName(name: String, character: CharacterModel): Future[UpdateCharacterDataResponse]
}
