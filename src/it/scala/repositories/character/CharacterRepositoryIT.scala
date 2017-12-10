package repositories.character

import it.helpers.IntegrationSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CharacterRepositoryIT extends IntegrationSuite {

  lazy val repository = new CharacterRepository

  "Calling the update character model method" must {
    "create a new character if one doesn't exist already" in {
      val character = CharacterModel("bob", 20)

      repository.createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(character.name, character))
        .flatMap(_ => repository.getRecordByName(character.name))
        .map(_ mustBe Some(character))
    }
    "update an existing character with a new level" in {
      val character = CharacterModel("bob", 20)
      val updatedCharacter = CharacterModel("bob", 21)

      repository.createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(character.name, updatedCharacter))
        .flatMap(_ => repository.getRecordByName(character.name))
        .map(_ mustBe Some(updatedCharacter))
    }
  }
}
