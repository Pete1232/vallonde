package components.updatecharacter.handler

import components.updatecharacter.repositories.MockCharacterUpdater

class MockUpdateCharacterDataHandler
  extends UpdateCharacterDataHandler(MockCharacterUpdater)
