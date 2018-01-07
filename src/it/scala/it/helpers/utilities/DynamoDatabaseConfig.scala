package it.helpers.utilities

import java.io.{File, FileInputStream, InputStreamReader}

import com.amazonaws.services.dynamodbv2.model._
import io.circe.generic.auto._
import io.circe.yaml

case class ParseableAttributeDefinitions(AttributeName: String, AttributeType: String)

case class ParseableKeySchema(AttributeName: String, KeyType: String)

case class ParseableProvisionedThroughput(ReadCapacityUnits: Long, WriteCapacityUnits: Long)

case class ParseableStreamSpecification(StreamViewType: String)

case class DynamoDatabaseConfig(TableName: String,
                                AttributeDefinitions: Seq[ParseableAttributeDefinitions],
                                KeySchema: Seq[ParseableKeySchema],
                                ProvisionedThroughput: ParseableProvisionedThroughput,
                                StreamSpecification: ParseableStreamSpecification) {

  lazy val asCreateTableRequest: CreateTableRequest = {
    new CreateTableRequest()
      .withTableName(TableName)
      .withAttributeDefinitions(attributeDefinitions: _*)
      .withKeySchema(keySchema: _*)
      .withProvisionedThroughput(new ProvisionedThroughput(ProvisionedThroughput.ReadCapacityUnits, ProvisionedThroughput.WriteCapacityUnits))
      .withStreamSpecification(new StreamSpecification().withStreamEnabled(true).withStreamViewType(StreamSpecification.StreamViewType))
  }
  private val attributeDefinitions: Seq[AttributeDefinition] = AttributeDefinitions.map { ad =>
    new AttributeDefinition()
      .withAttributeName(ad.AttributeName)
      .withAttributeType(ad.AttributeType)
  }
  private val keySchema: Seq[KeySchemaElement] = KeySchema.map { ks =>
    new KeySchemaElement()
      .withAttributeName(ks.AttributeName)
      .withKeyType(ks.KeyType)
  }
}

object DynamoDatabaseConfig {
  def fromCloudFormationTemplate: CreateTableRequest = {
    val template = new FileInputStream(new File("template.yaml"))

    yaml.parser.parse(new InputStreamReader(template))
      .flatMap{template =>
        template.hcursor.downField("Resources").downField("CharactersTable").downField("Properties").as[DynamoDatabaseConfig]
      }.right.get.asCreateTableRequest
  }
}
