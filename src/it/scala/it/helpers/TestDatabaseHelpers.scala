package it.helpers

import com.amazonaws.ClientConfiguration
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait TestDatabaseHelpers extends BeforeAndAfterEach {
  self: Suite =>

  lazy val config: Config = ConfigFactory.load()

  lazy val dynamoClient: AmazonDynamoDB = {

    val configBase = "akka.stream.alpakka.dynamodb"
    val host: String = config.getString(s"$configBase.host")
    val port: String = config.getString(s"$configBase.port")
    val region: String = config.getString(s"$configBase.region")

    val clientConfig = new ClientConfiguration()
    clientConfig.setConnectionTimeout(500)

    AmazonDynamoDBClientBuilder.standard()
      .withClientConfiguration(clientConfig)
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s"http://$host:$port", region))
      .build()
  }

  private def cleanUpDatabase(): Unit = {
    try {
      val tables: mutable.Buffer[String] = dynamoClient.listTables().getTableNames.asScala
      for (table <- tables) {
        dynamoClient.deleteTable(table).toString
      }
    } catch {
      case e: Throwable => cancel(e)
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    cleanUpDatabase()
  }

  override def afterEach(): Unit = {
    cleanUpDatabase()
    super.afterEach()
  }
}
