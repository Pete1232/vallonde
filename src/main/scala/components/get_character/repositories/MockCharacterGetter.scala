package components.get_character.repositories

import cats.data.EitherT
import repositories.character.models.{CharacterModel, StatsModel}

import scala.concurrent.Future

object MockCharacterGetter extends CharacterGetter {

  val mockResult: CharacterModel = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1))

  override def getRecordByName(name: String): EitherT[Future, String, CharacterModel] = {
    val futureResult: Future[Either[String, CharacterModel]] = {
      Future.successful(Right(mockResult))
    }
    EitherT(futureResult)
  }
}
