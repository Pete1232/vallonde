package repositories

import org.scalatest.Suites
import repositories.character.CharacterRepositoryIT

class RepositoryITs extends Suites(
  new CharacterRepositoryIT
)
