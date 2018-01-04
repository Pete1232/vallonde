package connectors.filestore

import scala.util.control.NoStackTrace

case class FileUploadResult(fileStore: FileStore.Value, metadata: Map[String, Any]) extends NoStackTrace
