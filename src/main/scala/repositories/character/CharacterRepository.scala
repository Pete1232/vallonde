package repositories.character

import cats.data.EitherT
import cats.implicits._
import com.amazonaws.services.dynamodbv2.model._
import components.get_character.repositories.CharacterGetter
import components.updatecharacter.repositories.CharacterUpdater
import repositories.character.models.{CharacterModel, UpdateCharacterDataResponse}

import scala.collection.convert.ImplicitConversionsToJava._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CharacterRepository()
                         (implicit ec: ExecutionContext) extends CharacterGetter with CharacterUpdater {

  import CharacterModel.AttributeNames._
  import DefaultDynamoClient._
  import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._

  val CHARACTER_TABLE_NAME = "character"

  override def getRecordByName(name: String): EitherT[Future, String, CharacterModel] = {
    EitherT.fromOptionF(
      client.single(
        new GetItemRequest()
          .withTableName(CHARACTER_TABLE_NAME)
          .withKey(Map(NAME -> new AttributeValue().withS(name)))
      ).map { result =>
        Try(CharacterModel.fromJavaMap(result.getItem)).getOrElse(None)
      }, "No character model found")
  }

  override def updateRecordByName(name: String, character: CharacterModel): Future[UpdateCharacterDataResponse] = {
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
