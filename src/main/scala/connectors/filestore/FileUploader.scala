package connectors.filestore

import java.nio.file.Path

import scala.concurrent.Future

trait FileUploader[To <: FileLocation] {
  def pushToStore(path: Path, to: To): Future[FileUploadResult]
}
