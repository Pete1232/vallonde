package repositories.character

import com.amazonaws.services.dynamodbv2.model._
import components.character.repositories.CharacterUpdater
import repositories.character.models.CharacterModel

import scala.collection.convert.ImplicitConversionsToJava._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CharacterRepository extends CharacterUpdater {

  import CharacterModel.AttributeNames._
  import CharacterRepository._
  import DefaultDynamoClient._
  import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._

  def createCharacterTable(): Future[CreateTableResult] = {

    val nameType: AttributeDefinition = new AttributeDefinition()
      .withAttributeName(NAME)
      .withAttributeType(ScalarAttributeType.S)

    client.single(
      new CreateTableRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withAttributeDefinitions(nameType)
        .withKeySchema(
          new KeySchemaElement()
            .withAttributeName(NAME)
            .withKeyType(KeyType.HASH)
        )
        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
    )
  }

  def getRecordByName(name: String)(implicit ec: ExecutionContext): Future[Option[CharacterModel]] = {
    client.single(
      new GetItemRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withKey(Map(NAME -> new AttributeValue().withS(name)))
    ).map { result =>
      Try(CharacterModel.fromJavaMap(result.getItem))
        .getOrElse(None)
    }
  }

  def updateRecordByName(name: String, character: CharacterModel): Future[UpdateItemResult] = {
    client.single(
      new UpdateItemRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withKey(Map(NAME -> new AttributeValue().withS(name)))
        .withAttributeUpdates(character.asAttributeValueUpdate)
    )
  }
}

object CharacterRepository {
  val CHARACTER_TABLE_NAME = "character"
}
