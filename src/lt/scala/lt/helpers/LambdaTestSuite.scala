package lt.helpers

import org.scalatest.{AsyncTestSuite, AsyncWordSpec, MustMatchers}

trait LambdaTestSuite extends AsyncWordSpec with MustMatchers with TestLambdaHelpers {
  self: AsyncTestSuite =>
}
