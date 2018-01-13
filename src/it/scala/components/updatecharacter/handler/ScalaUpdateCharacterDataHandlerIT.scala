package components.updatecharacter.handler

import com.amazonaws.services.lambda.model.{InvokeRequest, InvokeResult}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import it.helpers.LambdaTestSuite
import org.scalatest.DoNotDiscover
import repositories.character.models.{CharacterModel, StatsModel}

@DoNotDiscover
class ScalaUpdateCharacterDataHandlerIT extends LambdaTestSuite {

  "Calling the UpdateCharacter function" must {
    "run without throwing an error when valid data is sent" in {

      val inputJson: Json = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1)).asJson

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName("UpdateCharacter")
          .withPayload(inputJson.toString())
      )

      result.getStatusCode mustBe 200
    }
    "return the input value correctly" in {

      val inputModel: CharacterModel = CharacterModel("Test", 1, StatsModel(1, 1, 1, 1, 1, 1))

      val result: InvokeResult = lambda.invoke(
        new InvokeRequest()
          .withFunctionName("UpdateCharacter")
          .withPayload(inputModel.asJson.toString())
      )

      val resultAsString: String = new String(result.getPayload.array())

      parse(resultAsString).flatMap(_.as[CharacterModel]).right.get mustBe inputModel
    }
  }
}