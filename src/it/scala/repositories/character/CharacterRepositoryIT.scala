package repositories.character

import it.helpers.IntegrationSuite
import org.scalatest.DoNotDiscover
import repositories.character.models.{CharacterModel, StatsModel}

@DoNotDiscover
class CharacterRepositoryIT extends IntegrationSuite {

  lazy val repository = new CharacterRepository

  "Calling the update character model method followed by the get method" must {

    val defaultStats = StatsModel(1, 1, 1, 1, 1, 1)
    val defaultCharacter = CharacterModel("bob", 1, defaultStats)

    "create a new character if one doesn't exist already and return it" in {
      repository.createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, defaultCharacter))
        .flatMap(_ => repository.getRecordByName(defaultCharacter.name))
        .map(_ mustBe Some(defaultCharacter))
    }
    "update an existing character with a new level and return the updated model" in {
      val updatedCharacter = defaultCharacter.copy(level = 2)

      repository.createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, updatedCharacter))
        .flatMap(_ => repository.getRecordByName(defaultCharacter.name))
        .map(_ mustBe Some(updatedCharacter))
    }
    "return None if no character exists or was created" in {
      repository.createCharacterTable()
        .flatMap(_ => repository.getRecordByName(defaultCharacter.name))
        .map(_ mustBe None)
    }
  }
}
