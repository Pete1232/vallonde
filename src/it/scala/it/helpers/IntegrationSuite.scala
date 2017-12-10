package it.helpers

import org.scalatest.{AsyncTestSuite, AsyncWordSpec, MustMatchers}

trait IntegrationSuite extends AsyncWordSpec with MustMatchers with LocalDynamoDB {
  self: AsyncTestSuite =>
}
