package repositories.character

import com.amazonaws.services.dynamodbv2.model.{AttributeAction, AttributeValue, AttributeValueUpdate}
import repositories.character.CharacterModel.AttributeNames.LEVEL

import scala.language.implicitConversions

case class CharacterModel(name: String,
                          level: Int) {

  val asAttributeValueUpdate: java.util.Map[String, AttributeValueUpdate] = {

    import scala.collection.convert.ImplicitConversionsToJava._

    def update(value: AttributeValue) = {
      new AttributeValueUpdate()
        .withValue(value)
        .withAction(AttributeAction.PUT)
    }

    Map(LEVEL -> update(new AttributeValue().withN(level.toString)))
  }
}

object CharacterModel {

  object AttributeNames {
    val NAME = "name"
    val LEVEL = "level"
  }

  implicit def fromJavaMap(inputMap: java.util.Map[String, AttributeValue]): Option[CharacterModel] = {
    import AttributeNames._

    import scala.collection.convert.ImplicitConversionsToScala._

    val name = inputMap.toMap.get(NAME).map(_.getS)
    val level = inputMap.toMap.get(LEVEL).map(_.getN.toInt)

    for {
      n <- name
      l <- level
    } yield CharacterModel(n, l)
  }
}
