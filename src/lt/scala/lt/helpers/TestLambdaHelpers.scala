package lt.helpers

import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClient, AWSLambdaClientBuilder}
import config.amazon.{DefaultAmazonClientFactory, TypesafeAmazonConfigProvider}

object TestLambdaHelpers {

  lazy val lambda: AWSLambda = LambdaClientFactory.client

  private object DefaultLambdaConfigProvider extends TypesafeAmazonConfigProvider {
    override val configRoot: String = "aws.lambda"
  }

  private object LambdaClientFactory extends DefaultAmazonClientFactory[AWSLambdaClientBuilder, AWSLambda](DefaultLambdaConfigProvider) {
    override val defaultClient: AwsSyncClientBuilder[AWSLambdaClientBuilder, AWSLambda] = {
      AWSLambdaClient.builder()
    }
  }

  object Functions {
    val MOCK_UPDATE_CHARACTER = "MockUpdateCharacterData"
    val MOCK_GET_CHARACTER = "MockGetCharacterData"
  }

}
