package components.upload_serverless_website.handlers

import java.io.File
import java.nio.file.Path

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.upload_serverless_website.connectors.CodePipelineConnector
import components.upload_serverless_website.models.{S3Location, SuccessDetails}
import config.global.GlobalConfig
import connectors.filestore.{AwsFileLocation, FileDownloadResult, FileDownloader}
import io.circe.generic.auto._
import io.circe.optics.JsonPath._
import io.circe.parser._
import org.apache.logging.log4j.{LogManager, Logger}

import scala.concurrent.{Await, ExecutionContext, Future}

class UploadServerlessHandler(codePipelineConnector: CodePipelineConnector,
                              fileDownloader: FileDownloader[AwsFileLocation],
                              globalConfig: GlobalConfig)
                             (implicit ec: ExecutionContext) extends RequestHandler[String, String] {
  override def handleRequest(input: String, context: Context): String = {

    val logger: Logger = LogManager.getLogger(this.getClass)

    logger.debug("Starting website upload to S3")

    (for {
      doc <- parse(input).toOption
      job <- root.selectDynamic("CodePipeline.job").json.getOption(doc)
      jobId <- root.id.string.getOption(job)
      fileLocations <- {
        root.data.inputArtifacts.arr.getOption(job)
          .map { inputs =>
            inputs
              .flatMap(input => root.location.s3Location.as[S3Location].getOption(input))
              .map(location => AwsFileLocation(location.bucketName, location.objectKey))
          }
      }
    } yield {
      val result: Future[Vector[Path]] = Future.sequence {
        fileLocations
          .map { location =>
            fileDownloader.downloadFromStore(location, new File(s"/tmp/${location.key}").toPath)
          }
      }
      val unsafeResult: Seq[Path] = Await.result(result, globalConfig.futureTimeout)
      codePipelineConnector.sendSuccessEvent(jobId, SuccessDetails(jobId))
    }).fold(s"Error parsing JSON")(_ => input)
  }
}
