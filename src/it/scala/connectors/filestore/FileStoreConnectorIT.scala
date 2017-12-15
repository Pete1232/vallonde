package connectors.filestore

import java.io.File
import java.nio.file.Path

import connectors.filestore.alpakka.AlpakkaFileStoreConnector
import connectors.filestore.amazon.AmazonFileStoreConnector
import it.helpers.S3TestSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class FileStoreConnectorIT extends S3TestSuite {

  lazy val amazonConnector = new AmazonFileStoreConnector(defaultAmazonClientFactory)
  lazy val alpakkaConnector = new AlpakkaFileStoreConnector()

  val testPath: Path = new File("src/it/resources/files/simple.html").toPath
  val testDownloadPath: Path = {
    val downloadFile = new File("target/downloaded.html")
    downloadFile.deleteOnExit()
    downloadFile.toPath
  }

  val testLocation: AwsFileLocation = AwsFileLocation("testbucket", "testKey")

  "Calling the upload method" must {
    "upload the file at the given path to the s3 server and return metadata" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .map(_.metadata must not be Map.empty)
    }
  }
  "Calling the upload method followed by the download method" must {
    "download the uploaded file without failure" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .flatMap(_ => alpakkaConnector.downloadFromStore(testLocation, testDownloadPath))
        .map(_.isOkay mustBe true)
    }
    "correctly download the file without modification" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .flatMap(_ => alpakkaConnector.downloadFromStore(testLocation, testDownloadPath))
        .map(_ => computeHash(testPath) mustBe computeHash(testDownloadPath))
    }
  }
}
