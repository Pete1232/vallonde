package repositories.character

import com.amazonaws.services.dynamodbv2.model._
import components.updatecharacter.repositories.CharacterUpdater
import repositories.character.models.{CharacterModel, UpdateCharacterDataResponse}

import scala.collection.convert.ImplicitConversionsToJava._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CharacterRepository()
                         (implicit ec: ExecutionContext) extends CharacterUpdater {

  import CharacterModel.AttributeNames._
  import DefaultDynamoClient._
  import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._

  val CHARACTER_TABLE_NAME = "character"

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

  def updateRecordByName(name: String, character: CharacterModel): Future[UpdateCharacterDataResponse] = {
    client.single(
      new UpdateItemRequest()
        .withTableName(CHARACTER_TABLE_NAME)
        .withKey(Map(NAME -> new AttributeValue().withS(name)))
        .withAttributeUpdates(character.asAttributeValueUpdate)
    ).map(_ => None)
      .recover {
        case t: Throwable => Some(t.getMessage)
      }.map(UpdateCharacterDataResponse.apply)
  }
}
