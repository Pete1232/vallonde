package components.updatecharacter

import components.updatecharacter.handler.UpdateCharacterDataHandlerIT
import org.scalatest.Suites

class UpdateCharacterITs extends Suites(
  new UpdateCharacterDataHandlerIT
)
