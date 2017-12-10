package repositories.character

import it.helpers.IntegrationSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CharacterRepositoryIT extends IntegrationSuite {

  lazy val repository = new CharacterRepository

  "Calling get on the character table" must {
    "return the name of the character if an item exists" in {

      val expectedName = "bob"

      repository.createTable()
        .flatMap(_ => repository.updateRecord())
        .flatMap(_ => repository.getRecord)
        .map(_ mustBe Some(expectedName))
    }
    "return None if the item does not exist" in {

      repository.createTable()
        .flatMap(_ => repository.getRecord)
        .map(_ mustBe None)
    }
  }
}
