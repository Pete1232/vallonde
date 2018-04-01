package it.helpers

import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._
import com.amazonaws.services.dynamodbv2.model._
import com.typesafe.config.ConfigFactory
import it.helpers.utilities.DynamoDatabaseConfig
import org.scalatest.{BeforeAndAfterEach, Suite}
import repositories.character.DefaultDynamoClient

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait TestDatabaseHelpers extends BeforeAndAfterEach {
  self: Suite =>

  def createCharacterTable(): Future[CreateTableResult] = {
    DefaultDynamoClient.client.single(DynamoDatabaseConfig.fromCloudFormationTemplate)
  }

  private def cleanUpDatabase(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val tableNames: Future[mutable.Buffer[String]] = DefaultDynamoClient.client.single(
      new ListTablesRequest()
    ).map(_.getTableNames.asScala)

    val deleteResult: Future[mutable.Buffer[DeleteTableResult]] = tableNames.flatMap { tables =>
      Future.sequence(
        for {
          table <- tables
        } yield DefaultDynamoClient.client.single(new DeleteTableRequest(table))
      )
    }.recover {
      case e: Throwable => cancel(e)
    }

    Await.result(deleteResult, Duration.Inf)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    System.setProperty("aws.accessKeyId", "test")
    System.setProperty("aws.secretKey", "test")
    ConfigFactory.invalidateCaches()
    cleanUpDatabase()
  }

  override def afterEach(): Unit = {
    cleanUpDatabase()
    super.afterEach()
  }
}
