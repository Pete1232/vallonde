package components

import components.get_character.handler.GetCharacterDataHandlerLT
import components.updatecharacter.handler.UpdateCharacterDataHandlerIT
import lt.helpers.TestLambdaSetup
import org.scalatest.Suites

class LambdaTestSuites extends Suites(
  new GetCharacterDataHandlerLT,
  new UpdateCharacterDataHandlerIT
) with TestLambdaSetup
