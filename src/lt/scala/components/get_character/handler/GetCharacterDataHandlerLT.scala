package components.get_character.handler

import com.amazonaws.services.lambda.model.{InvokeRequest, InvokeResult}
import components.get_character.repositories.MockCharacterGetter
import io.circe.generic.auto._
import io.circe.syntax._
import lt.helpers.LambdaTestSuite
import org.scalatest.DoNotDiscover
import play.twirl.api.utils.StringEscapeUtils
import repositories.character.models.CharacterModel

@DoNotDiscover
class GetCharacterDataHandlerLT extends LambdaTestSuite {

  "Calling the MOCKED version of the GetCharacter function" must {
    "return 200 status when called" in {

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_GET_CHARACTER)
          .withPayload("""{"path" : "character/Test", "httpMethod" : "GET"}""")
      )

      result.getStatusCode mustBe 200
    }
    "return character data if it was found" in {
      val inputModel: CharacterModel = MockCharacterGetter.mockResult
      val escapedResponseWithValidBody: String = StringEscapeUtils.escapeEcmaScript(inputModel.asJson.noSpaces)

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName(Functions.MOCK_GET_CHARACTER)
          .withPayload("""{"path" : "character/Test", "httpMethod" : "GET"}""")
      )

      val resultAsString: String = new String(result.getPayload.array())

      resultAsString must include(escapedResponseWithValidBody)
    }
  }
}
