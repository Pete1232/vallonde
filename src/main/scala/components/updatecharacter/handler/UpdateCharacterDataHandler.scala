package components.updatecharacter.handler

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import components.updatecharacter.repositories.CharacterUpdater
import io.circe._
import io.circe.generic.auto._
import org.apache.logging.log4j.{LogManager, Logger}
import repositories.character.models.CharacterModel

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

class UpdateCharacterDataHandler(characterUpdater: CharacterUpdater) extends RequestStreamHandler {

  val logger: Logger = LogManager.getLogger(this.getClass)

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {

    val inputString: String = Source.fromInputStream(input).mkString

    // Testing cloudwatch
    logger.debug(s"Received update request $inputString")
    System.out.println(s"Received update request $inputString")

    val asyncResult: Future[Option[String]] = parser.parse(inputString)
      .flatMap(_.as[CharacterModel])
      .fold(handleInputError, handleInputSuccess)

    val unsafeSyncResult: String = Await.result(asyncResult, Duration.Inf).getOrElse(inputString)

    output.write(unsafeSyncResult.getBytes())
  }

  private def handleInputError(error: Error): Future[Option[String]] = {
    Future.successful(Some(error match {
      case p: ParsingFailure => s"ParsingFailure: ${p.message}"
      case d: DecodingFailure => s"DecodingFailure: ${d.message}"
      case e => s"${e.getClass.getSimpleName}: ${e.getMessage}"
    }))
  }

  private def handleInputSuccess(input: CharacterModel): Future[Option[String]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    characterUpdater.updateRecordByName(input.name, input)
      .map(_.errors)
      .recover {
        case e: Throwable => Some(e.getMessage)
      }
  }
}
