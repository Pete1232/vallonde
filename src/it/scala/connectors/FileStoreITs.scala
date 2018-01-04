package connectors

import connectors.filestore.FileStoreConnectorIT
import org.scalatest.Suites

class FileStoreITs extends Suites(
  new FileStoreConnectorIT
)
