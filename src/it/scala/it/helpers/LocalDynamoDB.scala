package it.helpers

import com.amazonaws.ClientConfiguration
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.collection.JavaConverters._

trait LocalDynamoDB extends BeforeAndAfterEach {
  self: Suite =>

  lazy val config: Config = ConfigFactory.load()

  lazy val dynamoClient: AmazonDynamoDB = {

    val configBase = "akka.stream.alpakka.dynamodb"
    val host = config.getString(s"$configBase.host")
    val port = config.getString(s"$configBase.port")
    val region = config.getString(s"$configBase.region")

    val clientConfig = new ClientConfiguration()
    clientConfig.setConnectionTimeout(500)

    AmazonDynamoDBClientBuilder.standard()
      .withClientConfiguration(clientConfig)
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s"http://$host:$port", region))
      .build()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    cleanUpDatabase()
  }

  private def cleanUpDatabase(): Unit = {
    try {
      val tables = dynamoClient.listTables().getTableNames.asScala
      for (table <- tables) {
        dynamoClient.deleteTable(table).toString
      }
    } catch {
      case e: Throwable => cancel(e)
    }
  }

  override def afterEach(): Unit = {
    cleanUpDatabase()
    super.afterEach()
  }
}
