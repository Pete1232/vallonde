package connectors.filestore

import java.nio.file.Path

import scala.concurrent.Future

trait FileDownloader[From <: FileLocation] {
  def downloadFromStore(from: From, to: Path): Future[FileDownloadResult]
}
