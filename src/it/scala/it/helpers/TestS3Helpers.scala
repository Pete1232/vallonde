package it.helpers

import java.io.FileInputStream
import java.nio.file.Path
import java.security.{DigestInputStream, MessageDigest}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.s3.model.{AmazonS3Exception, DeleteObjectsRequest}
import connectors.filestore.amazon.S3ClientFactory
import it.helpers.utilities.S3CharacterConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

trait TestS3Helpers extends BeforeAndAfterEach with BeforeAndAfterAll {
  self: Suite =>

  lazy val defaultAmazonClientFactory: S3ClientFactory.type = S3ClientFactory

  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val bucketConfig: S3CharacterConfig = S3CharacterConfig.fromCloudFormationTemplate

  override def beforeAll(): Unit = {
    super.beforeAll()
    createCharacterBucket()
  }

  private def createCharacterBucket(): Unit = {
    import defaultAmazonClientFactory.s3Client._

    Try(createBucket(bucketConfig.asCreateBucketRequest))
      .recover {
        case e: AmazonS3Exception if e.getErrorCode == "BucketAlreadyExists" =>
          clearCharacterBucket()
          deleteBucket(bucketConfig.BucketName)
          createBucket(bucketConfig.asCreateBucketRequest)
      }.map { _ =>
      setBucketLifecycleConfiguration(bucketConfig.asLifecycleConfigurationRequest)
    }.get
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    defaultAmazonClientFactory.s3Client.deleteBucket(bucketConfig.BucketName)
  }

  def computeHash(path: Path): String = {
    val buffer = new Array[Byte](8192)
    val md5: MessageDigest = MessageDigest.getInstance("MD5")

    val dis = new DigestInputStream(new FileInputStream(path.toFile), md5)
    try {
      while (dis.read(buffer) != -1) {}
    } finally {
      dis.close()
    }

    md5.digest.map("%02x".format(_)).mkString
  }

  override def afterEach(): Unit = {
    clearCharacterBucket()
    super.afterEach()
  }

  private def clearCharacterBucket(): Unit = {
    import defaultAmazonClientFactory.s3Client._

    val keys: mutable.Buffer[String] = listObjectsV2(bucketConfig.BucketName)
      .getObjectSummaries
      .asScala
      .map(_.getKey)

    deleteObjects(
      new DeleteObjectsRequest(bucketConfig.BucketName)
        .withKeys(keys: _*)
    )
  }
}
