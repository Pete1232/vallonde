package components.updatecharacter.handler

import repositories.character.CharacterRepository

class DefaultUpdateCharacterDataHandler
  extends UpdateCharacterDataHandler(new CharacterRepository()(scala.concurrent.ExecutionContext.global))
