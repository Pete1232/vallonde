package components.updatecharacter.handler

import com.amazonaws.services.lambda.model.{InvokeRequest, InvokeResult, LogType}
import io.circe.generic.auto._
import io.circe.syntax._
import lt.helpers.LambdaTestSuite
import org.apache.logging.log4j.{LogManager, Logger}
import org.scalatest.DoNotDiscover
import play.twirl.api.utils.StringEscapeUtils
import repositories.character.models.{CharacterModel, StatsModel}

@DoNotDiscover
class UpdateCharacterDataHandlerIT extends LambdaTestSuite {

  val logger: Logger = LogManager.getLogger(this.getClass)

  "Calling the MOCKED version of the UpdateCharacter function" must {
    "return 200 status when valid data is sent" in {

      val inputModel: CharacterModel = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1))
      val escapedRequestWithValidBody: String = StringEscapeUtils.escapeEcmaScript(inputModel.asJson.noSpaces)

      val requestWithValidBody: String = s"""{"body": "$escapedRequestWithValidBody"}"""

      logger.debug(s"Making request $requestWithValidBody")

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_UPDATE_CHARACTER)
          .withPayload(requestWithValidBody)
      )

      result.getStatusCode mustBe 200
    }
    "return the input value correctly" in {

      val inputModel: CharacterModel = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1))
      val escapedRequestWithValidBody: String = StringEscapeUtils.escapeEcmaScript(inputModel.asJson.noSpaces)

      val requestWithValidBody: String = s"""{"body": "$escapedRequestWithValidBody"}"""

      logger.debug(s"Making request $requestWithValidBody")

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_UPDATE_CHARACTER)
          .withPayload(requestWithValidBody.toString)
          .withLogType(LogType.None)
      )

      logger.debug(result.toString)

      val resultAsString: String = new String(result.getPayload.array())

      logger.debug(s"Output : $resultAsString : End Output")

      resultAsString must include(escapedRequestWithValidBody)
    }
  }
}
