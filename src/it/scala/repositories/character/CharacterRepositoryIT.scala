package repositories.character

import it.helpers.DatabaseTestSuite
import org.scalatest.DoNotDiscover
import repositories.character.models.{CharacterModel, StatsModel, UpdateCharacterDataResponse}

import scala.concurrent.Future

@DoNotDiscover
class CharacterRepositoryIT extends DatabaseTestSuite {

  lazy val repository: CharacterRepository = new CharacterRepository()

  val defaultStats = StatsModel(1, 1, 1, 1, 1, 1)
  val defaultCharacter = CharacterModel("bob", 1, defaultStats)

  "Calling the get character method" must {
    "return None if no character exists or was created" in {
      createCharacterTable()
        .flatMap(_ => repository.getRecordByName(defaultCharacter.name).value)
        .map(_ mustBe Left("No character model found"))
    }
  }

  "Calling the update character method" must {
    "return None if a new character was created" in {
      createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, defaultCharacter))
        .map(_ mustBe UpdateCharacterDataResponse(None))
    }
    "return None if an existing character was updated" in {
      createCharacterTable()
        .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, defaultCharacter))
        .map(_ mustBe UpdateCharacterDataResponse(None))
    }
    "return an error if the character table does not exist" in {
      repository.updateRecordByName(defaultCharacter.name, defaultCharacter.copy(name = "not_bob"))
        .map(_.errors.get must include("Cannot do operations on a non-existent table"))
    }
  }

  "Calling the update character model method followed by the get method" must {
    "create a new character if one doesn't exist already and return it" in {

      lazy val insert: Future[UpdateCharacterDataResponse] = {
        createCharacterTable()
          .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, defaultCharacter))
      }

      lazy val get: Future[Either[String, CharacterModel]] = {
        repository.getRecordByName(defaultCharacter.name).value
      }

      for {
        _ <- insert
        result <- get
      } yield result mustBe Right(defaultCharacter)
    }
    "update an existing character with a new level and return the updated model" in {
      val updatedCharacter: CharacterModel = defaultCharacter.copy(level = 2)

      lazy val insert: Future[UpdateCharacterDataResponse] = {
        createCharacterTable()
          .flatMap(_ => repository.updateRecordByName(defaultCharacter.name, defaultCharacter))
      }

      lazy val update: Future[UpdateCharacterDataResponse] = {
        repository.updateRecordByName(defaultCharacter.name, updatedCharacter)
      }

      lazy val get: Future[Either[String, CharacterModel]] = {
        repository.getRecordByName(defaultCharacter.name).value
      }

      for {
        _ <- insert
        _ <- update
        result <- get
      } yield result mustBe Right(updatedCharacter)
    }
  }
}
