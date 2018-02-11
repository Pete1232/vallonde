package lt.helpers

import com.amazonaws.services.lambda.model.{CreateFunctionRequest, DeleteFunctionRequest}
import lt.helpers.TestLambdaHelpers._
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.JavaConverters._

trait TestLambdaSetup extends BeforeAndAfterAll {
  this: Suite =>

  override def beforeAll(): Unit = {
    super.beforeAll()
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
