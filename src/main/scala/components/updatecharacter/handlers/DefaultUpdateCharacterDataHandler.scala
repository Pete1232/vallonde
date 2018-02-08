package components.updatecharacter.handlers

import repositories.character.CharacterRepository

class DefaultUpdateCharacterDataHandler
  extends UpdateCharacterDataHandlerInternal(new CharacterRepository()(scala.concurrent.ExecutionContext.global))
