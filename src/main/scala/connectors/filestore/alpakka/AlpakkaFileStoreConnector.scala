package connectors.filestore.alpakka

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.S3Settings
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl.FileIO
import connectors.filestore._

import scala.concurrent.{ExecutionContext, Future}

class AlpakkaFileStoreConnector()
                               (implicit mat: ActorMaterializer, ec: ExecutionContext)
  extends FileDownloader[AwsFileLocation] {

  implicit val system: ActorSystem = mat.system

  val s3Settings: S3Settings = S3Settings()
  val s3Client: S3Client = new S3Client(s3Settings)

  override def downloadFromStore(from: AwsFileLocation, to: Path): Future[Path] = {
    val parent = to.toFile.getParentFile
    if(!parent.exists()) parent.mkdirs()
    s3Client.download(from.bucket, from.key)
      .runWith(FileIO.toPath(to))
      .map { _ => to }
  }
}
