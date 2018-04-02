package connectors.filestore.amazon

import java.nio.file.Path

import com.amazonaws.services.s3.model.{AccessControlList, PutObjectRequest}
import connectors.filestore._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class AmazonFileStoreConnector(amazonClientFactory: S3ClientFactory)
                              (implicit ec: ExecutionContext)
  extends FileUploader[AwsFileLocation] {

  override def pushToStore(from: Path, to: AwsFileLocation): Future[FileUploadResult] = {

    val acl = new AccessControlList()
    to.acl.foreach(permit => acl.grantPermission(permit.grantee, permit.permission))

    val putRequest = new PutObjectRequest(to.bucket, to.key, from.toFile)
      .withAccessControlList(acl)

    Future(amazonClientFactory.client.putObject(putRequest))
      .map { result =>
        FileUploadResult(FileStore.S3, result.getMetadata.getRawMetadata.asScala.toMap)
      }
  }
}
