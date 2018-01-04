package it.helpers

import java.io.FileInputStream
import java.nio.file.Path
import java.security.{DigestInputStream, MessageDigest}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.s3.model.{CreateBucketRequest, DeleteObjectsRequest}
import connectors.filestore.amazon.{DefaultAmazonClientFactory, DefaultAmazonConfigProvider}
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait TestS3Helpers extends BeforeAndAfterEach {
  self: Suite =>

  lazy val defaultAmazonClientFactory = new DefaultAmazonClientFactory(defaultAmazonConfig)
  private lazy val defaultAmazonConfig = new DefaultAmazonConfigProvider

  implicit val system: ActorSystem = ActorSystem("TestSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def beforeEach(): Unit = {
    super.beforeEach()

    val createRequest = new CreateBucketRequest("testbucket")
    defaultAmazonClientFactory.client.createBucket(createRequest)
  }

  override def afterEach(): Unit = {
    val keys: mutable.Buffer[String] = defaultAmazonClientFactory.client.listObjectsV2("testbucket")
      .getObjectSummaries
      .asScala
      .map(_.getKey)

    defaultAmazonClientFactory.client.deleteObjects(
      new DeleteObjectsRequest("testbucket")
        .withKeys(keys: _*)
    )
    defaultAmazonClientFactory.client.deleteBucket("testbucket")
    super.afterEach()
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
}
