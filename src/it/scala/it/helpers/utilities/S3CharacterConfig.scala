package it.helpers.utilities

import java.io.{File, FileInputStream, InputStreamReader}

import com.amazonaws.services.s3.model._
import io.circe.generic.auto._
import io.circe.yaml

import scala.collection.JavaConverters._

case class LifecycleRules(NoncurrentVersionExpirationInDays: Int, Status: String)

case class ParseableLifecycleConfiguration(Rules: Seq[LifecycleRules])

case class ParseableVersioningConfiguration(Status: String)

case class ParseableWebsiteConfiguration(ErrorDocument: String, IndexDocument: String)

case class S3CharacterConfig(AccessControl: String,
                             BucketName: String,
                             LifecycleConfiguration: ParseableLifecycleConfiguration,
                             VersioningConfiguration: ParseableVersioningConfiguration) {
  lazy val asCreateBucketRequest: CreateBucketRequest = {
    new CreateBucketRequest(BucketName)
      .withCannedAcl(CannedAccessControlList.valueOf(AccessControl))
  }

  lazy val asLifecycleConfigurationRequest: SetBucketLifecycleConfigurationRequest = {

    val config: BucketLifecycleConfiguration = new BucketLifecycleConfiguration()
      .withRules(
        LifecycleConfiguration.Rules.map { rule =>
          new BucketLifecycleConfiguration.Rule()
            .withStatus(rule.Status)
            .withNoncurrentVersionExpirationInDays(rule.NoncurrentVersionExpirationInDays)
        }.asJava
      )

    new SetBucketLifecycleConfigurationRequest(BucketName, config)
  }

  lazy val asVersioningConfigurationRequest: SetBucketVersioningConfigurationRequest = {

    val config: BucketVersioningConfiguration = new BucketVersioningConfiguration()
      .withStatus(VersioningConfiguration.Status)

    new SetBucketVersioningConfigurationRequest(BucketName, config)
  }
}

object S3CharacterConfig {
  def fromCloudFormationTemplate: S3CharacterConfig = {
    val template = new FileInputStream(new File("template.yaml"))

    yaml.parser.parse(new InputStreamReader(template))
      .flatMap { template =>
        template.hcursor.downField("Resources").downField("CharacterPageBucket").downField("Properties").as[S3CharacterConfig]
      }.right.get
  }
}
