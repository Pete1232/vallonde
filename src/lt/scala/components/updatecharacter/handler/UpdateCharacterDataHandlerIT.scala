package components.updatecharacter.handler

import com.amazonaws.services.lambda.model.{InvocationType, InvokeRequest, InvokeResult, LogType}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import lt.helpers.LambdaTestSuite
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalatest.DoNotDiscover
import repositories.character.models.{CharacterModel, StatsModel}

@DoNotDiscover
class UpdateCharacterDataHandlerIT extends LambdaTestSuite {

  val logger: Logger = LogManager.getLogger(this.getClass)

  "Calling the MOCKED version of the UpdateCharacter function" must {
    "run without throwing an error when valid data is sent" in {

      val inputJson: Json = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1)).asJson

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_UPDATE_CHARACTER)
          .withPayload(inputJson.toString())
      )

      result.getStatusCode mustBe 200
    }
    "return the input value correctly" ignore {

      val inputModel: CharacterModel = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1))

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_UPDATE_CHARACTER)
          .withPayload(inputModel.asJson.toString())
          .withInvocationType(InvocationType.RequestResponse)
          .withLogType(LogType.None)
      )

      logger.debug(result.toString)

      val resultAsString: String = new String(result.getPayload.array())

      logger.debug(s"Output : $resultAsString : End Output")

      parse(resultAsString).flatMap(_.as[CharacterModel]) match {
        case Left(error) => fail(error.getMessage)
        case Right(model) => model mustBe inputModel
      }
    }
  }
}
