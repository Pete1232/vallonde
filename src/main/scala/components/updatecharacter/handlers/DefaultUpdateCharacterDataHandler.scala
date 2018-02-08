package components.updatecharacter.handlers

import repositories.character.CharacterRepository

class DefaultUpdateCharacterDataHandler
  extends UpdateCharacterDataHandler(new CharacterRepository()(scala.concurrent.ExecutionContext.global))
