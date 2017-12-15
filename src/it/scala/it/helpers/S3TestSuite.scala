package it.helpers

import org.scalatest.{AsyncTestSuite, AsyncWordSpec, MustMatchers}

trait S3TestSuite extends AsyncWordSpec with MustMatchers with TestS3Helpers {
  self: AsyncTestSuite =>
}
