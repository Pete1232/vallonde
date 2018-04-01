package lt.helpers

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest}
import com.typesafe.config.ConfigFactory
import lt.helpers.TestLambdaHelpers._
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._

trait TestLambdaSetup extends BeforeAndAfterAll {
  this: Suite =>

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("aws.accessKeyId", "test")
    System.setProperty("aws.secretKey", "test")
    ConfigFactory.invalidateCaches()
    deleteAllFunctions()
    createUpdateCharacterFunction(isMocked = true)
    createGetCharacterFunction(isMocked = true)
  }

  private def createUpdateCharacterFunction(isMocked: Boolean) = {
    val config: LambdaFunctionConfig = LambdaFunctionConfig.fromCloudFormationTemplate("UpdateCharacterDataFunction")
    val createRequest: CreateFunctionRequest = {
      config.asCreateFunctionRequest(isMocked)
    }

    lambda.createFunction(createRequest)
  }

  private def createGetCharacterFunction(isMocked: Boolean) = {
    val config: LambdaFunctionConfig = LambdaFunctionConfig.fromCloudFormationTemplate("GetCharacterDataFunction")
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
}
