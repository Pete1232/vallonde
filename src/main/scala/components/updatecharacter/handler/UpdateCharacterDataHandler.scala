package components.updatecharacter.handler

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import components.updatecharacter.repositories.CharacterUpdater
import io.circe._
import io.circe.generic.auto._
import repositories.character.models.CharacterModel

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class UpdateCharacterDataHandler(characterUpdater: CharacterUpdater) extends RequestStreamHandler {

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {

    val inputString: String = Source.fromInputStream(input).mkString

    val result: String = parser.parse(inputString)
      .flatMap(_.as[CharacterModel])
      .map { input =>
        Await.result(
          characterUpdater.updateRecordByName(input.name, input)
          , Duration.Inf)
      }.fold(_.getMessage, _.errors.getOrElse(inputString))

    output.write(result.getBytes())
  }
}
