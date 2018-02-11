package components.get_character.repositories

import cats.data.EitherT
import repositories.character.models.CharacterModel

import scala.concurrent.Future

trait CharacterGetter {
  def getRecordByName(name: String): EitherT[Future, String, CharacterModel]
}
