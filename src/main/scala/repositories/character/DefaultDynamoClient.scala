package repositories.character

import akka.stream.alpakka.dynamodb.impl.DynamoSettings
import akka.stream.alpakka.dynamodb.scaladsl.DynamoClient

object DefaultDynamoClient {

  import utilities.DefaultActorSystem._

  val settings: DynamoSettings = DynamoSettings(system)
  val client: DynamoClient = DynamoClient(settings)
}
