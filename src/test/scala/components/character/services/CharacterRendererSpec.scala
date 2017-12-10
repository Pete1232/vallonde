package components.character.services

import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{AsyncWordSpec, MustMatchers}
import repositories.character.models.{CharacterModel, StatsModel}

class CharacterRendererSpec extends AsyncWordSpec with MustMatchers with GeneratorDrivenPropertyChecks {

  "The character renderer" must {
    "show the character name as an <h1>" in {
      forAll(Gen.alphaNumStr) { name: String =>

        val model = CharacterModel(name, 1, StatsModel(1, 1, 1, 1, 1, 1))

        CharacterRenderer.render(model) must include(s"<h1>$name</h1>")
      }
    }
  }
}
