package lt.helpers

import com.amazonaws.services.lambda.AWSLambda
import org.scalatest.{AsyncTestSuite, AsyncWordSpec, MustMatchers}

trait LambdaTestSuite extends AsyncWordSpec with MustMatchers {
  self: AsyncTestSuite =>

  lazy val lambda: AWSLambda = TestLambdaHelpers.lambda

  lazy val Functions: TestLambdaHelpers.Functions.type = TestLambdaHelpers.Functions
}
