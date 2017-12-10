package repositories.character

import com.amazonaws.services.dynamodbv2.model._
import components.character.repositories.CharacterUpdater

import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.convert.ImplicitConversionsToScala._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CharacterRepository extends CharacterUpdater {

  import CharacterRepository._
  import DefaultDynamoClient._
  import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._

  val nameType: AttributeDefinition = new AttributeDefinition()
    .withAttributeName("name")
    .withAttributeType(ScalarAttributeType.S)

  def createTable(): Future[CreateTableResult] = {
    client.single(
      new CreateTableRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withAttributeDefinitions(nameType)
        .withKeySchema(
          new KeySchemaElement()
            .withAttributeName("name")
            .withKeyType(KeyType.HASH)
        )
        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
    )
  }

  def getRecord()(implicit ec: ExecutionContext): Future[Option[String]] = {
    client.single(
      new GetItemRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withKey(Map("name" -> new AttributeValue().withS("bob")))
    ).map { result =>
      Try(result.getItem.toMap.get("name"))
        .getOrElse(None)
        .map(_.getS)
    }
  }

  new GetItemResult

  def updateRecord(): Future[UpdateItemResult] = {
    client.single(
      new UpdateItemRequest(
        CHARACTER_TABLE_NAME,
        Map("name" -> new AttributeValue().withS("bob")),
        Map("level" -> new AttributeValueUpdate()
          .withValue(
            new AttributeValue().withN("20")
          )
          .withAction(AttributeAction.PUT)
        )
      )
    )
  }
}

object CharacterRepository {
  val CHARACTER_TABLE_NAME = "character"
}
