package repositories.character.models

import com.amazonaws.services.dynamodbv2.model.{AttributeAction, AttributeValue, AttributeValueUpdate}
import repositories.character.models.CharacterModel.AttributeNames._

case class CharacterModel(name: String,
                          level: Int,
                          stats: StatsModel) {

  val asAttributeValueUpdate: java.util.Map[String, AttributeValueUpdate] = {

    import scala.collection.convert.ImplicitConversionsToJava._

    def update(value: AttributeValue): AttributeValueUpdate = {
      new AttributeValueUpdate()
        .withValue(value)
        .withAction(AttributeAction.PUT)
    }

    Map(
      LEVEL -> update(new AttributeValue().withN(level.toString)),
      STATS -> update(new AttributeValue().withM(stats.asAttributeValues))
    )
  }
}

object CharacterModel {

  def fromJavaMap(inputMap: java.util.Map[String, AttributeValue]): Option[CharacterModel] = {
    import AttributeNames._

    import scala.collection.convert.ImplicitConversionsToScala._

    val name: Option[String] = inputMap.toMap.get(NAME).map(_.getS)
    val level: Option[Int] = inputMap.toMap.get(LEVEL).map(_.getN.toInt)
    val stats: Option[StatsModel] = inputMap.toMap.get(STATS).map(_.getM).flatMap(StatsModel.fromJavaMap)

    for {
      n <- name
      l <- level
      s <- stats
    } yield CharacterModel(n, l, s)
  }

  object AttributeNames {
    val NAME = "name"
    val LEVEL = "level"
    val STATS = "stats"
  }
}
