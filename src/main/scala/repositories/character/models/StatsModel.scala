package repositories.character.models

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import repositories.character.models.StatsModel.AttributeNames._

case class StatsModel(strength: Byte,
                      dexterity: Byte,
                      constitution: Byte,
                      intelligence: Byte,
                      wisdom: Byte,
                      charisma: Byte) {

  val asAttributeValues: Map[String, AttributeValue] = {
    Map(
      STR -> new AttributeValue().withN(strength.toString),
      DEX -> new AttributeValue().withN(dexterity.toString),
      CON -> new AttributeValue().withN(constitution.toString),
      INT -> new AttributeValue().withN(intelligence.toString),
      WIS -> new AttributeValue().withN(wisdom.toString),
      CHR -> new AttributeValue().withN(charisma.toString)
    )
  }
}

object StatsModel {

  def fromJavaMap(inputMap: java.util.Map[String, AttributeValue]): Option[StatsModel] = {
    import AttributeNames._

    import scala.collection.convert.ImplicitConversionsToScala._

    val str = inputMap.toMap.get(STR).map(_.getN.toByte)
    val dex = inputMap.toMap.get(DEX).map(_.getN.toByte)
    val con = inputMap.toMap.get(CON).map(_.getN.toByte)
    val int = inputMap.toMap.get(INT).map(_.getN.toByte)
    val wis = inputMap.toMap.get(WIS).map(_.getN.toByte)
    val chr = inputMap.toMap.get(CHR).map(_.getN.toByte)

    for {
      s <- str
      d <- dex
      cn <- con
      i <- int
      w <- wis
      cr <- chr
    } yield StatsModel(s, d, cn, i, w, cr)
  }

  object AttributeNames {
    val STR = "strength"
    val DEX = "dexterity"
    val CON = "constitution"
    val INT = "intelligence"
    val WIS = "wisdom"
    val CHR = "charisma"
  }

}
