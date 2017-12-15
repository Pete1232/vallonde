package it.helpers

import org.scalatest.{AsyncTestSuite, AsyncWordSpec, MustMatchers}

trait DatabaseTestSuite extends AsyncWordSpec with MustMatchers with TestDatabaseHelpers {
  self: AsyncTestSuite =>
}
