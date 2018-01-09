package components.updatecharacter.services

import repositories.character.models.CharacterModel

object CharacterRenderer {

  def render(characterModel: CharacterModel): String = {
    html.character(characterModel).body
  }
}
