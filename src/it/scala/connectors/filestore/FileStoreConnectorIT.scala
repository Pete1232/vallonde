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

  val testLocation: AwsFileLocation = AwsFileLocation(bucketConfig.BucketName, "testKey")

  "Calling the upload method" must {
    "upload the file at the given path to the s3 server and return metadata" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .map(_.metadata must not be Map.empty)
    }
  }
  "Calling the upload method followed by the download method" must {
    "download the uploaded file without failure and return the file location" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .flatMap(_ => alpakkaConnector.downloadFromStore(testLocation, testDownloadPath))
        .map(_ mustBe testDownloadPath)
    }
    "correctly download the file without modification" in {

      amazonConnector.pushToStore(testPath, testLocation)
        .flatMap(_ => alpakkaConnector.downloadFromStore(testLocation, testDownloadPath))
        .map(_ => computeHash(testPath) mustBe computeHash(testDownloadPath))
    }
    "correctly download the file into a directory that didn't previously exist" in {

      val testLongDownloadPath: Path = {
        val downloadFile = new File("target/Vallonde/MyApp/Test")
        downloadFile.deleteOnExit()
        downloadFile.toPath
      }

      amazonConnector.pushToStore(testPath, testLocation)
        .flatMap(_ => alpakkaConnector.downloadFromStore(testLocation, testLongDownloadPath))
        .map { _ =>
          testLongDownloadPath.toFile.exists() && !testLongDownloadPath.toFile.isDirectory mustBe true
        }
    }
  }
}
