package lt.helpers

import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClient, AWSLambdaClientBuilder}
import config.amazon.{DefaultAmazonClientFactory, TypesafeAmazonConfigProvider}
import lt.helpers.utilities.LambdaUpdateCharacterDataConfig
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._

trait TestLambdaHelpers extends BeforeAndAfterAll {
  self: Suite =>

  lazy val lambda: AWSLambda = LambdaClientFactory.client

  object DefaultLambdaConfigProvider extends TypesafeAmazonConfigProvider {
    override val configRoot: String = "aws.lambda"
  }

  object LambdaClientFactory extends DefaultAmazonClientFactory[AWSLambdaClientBuilder, AWSLambda](DefaultLambdaConfigProvider) {
    override val defaultClient: AwsSyncClientBuilder[AWSLambdaClientBuilder, AWSLambda] = {
      AWSLambdaClient.builder()
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    deleteAllFunctions()
    createUpdateCharacterFunction(isMocked = false)
    createUpdateCharacterFunction(isMocked = true)
  }

  private def createUpdateCharacterFunction(isMocked: Boolean) = {
    val config: LambdaUpdateCharacterDataConfig = LambdaUpdateCharacterDataConfig.fromCloudFormationTemplate
    val createRequest: CreateFunctionRequest = {
      config.asCreateFunctionRequest(isMocked)
    }

    lambda.createFunction(createRequest)
  }

  private def deleteAllFunctions() = {
    lambda.listFunctions().getFunctions.asScala
      .map { function =>
        lambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(function.getFunctionName))
      }
  }

  override def afterAll(): Unit = {
    deleteAllFunctions()
    super.afterAll()
  }

  object Functions {
    val DEFAULT_UPDATE_CHARACTER = "UpdateCharacterData"
    val MOCK_UPDATE_CHARACTER = "MockUpdateCharacterData"
  }

}
