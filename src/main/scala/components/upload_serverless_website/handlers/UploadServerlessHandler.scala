package components.upload_serverless_website.handlers

import java.io.{File, FileOutputStream}

import com.amazonaws.SdkClientException
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.model.{GroupGrantee, Permission}
import components.upload_serverless_website.connectors.CodePipelineConnector
import components.upload_serverless_website.models.{FailureEventDetails, S3Location, SuccessEventDetails}
import config.global.GlobalConfig
import connectors.filestore._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.optics.JsonPath._
import io.circe.parser._
import net.lingala.zip4j.core.ZipFile
import org.apache.logging.log4j.{LogManager, Logger}

import scala.concurrent.{Await, ExecutionContext, Future}

class UploadServerlessHandler(codePipelineConnector: CodePipelineConnector,
                              fileDownloader: FileDownloader[AwsFileLocation],
                              fileUploader: FileUploader[AwsFileLocation],
                              globalConfig: GlobalConfig)
                             (implicit ec: ExecutionContext) {
  def handleRequest(input: String, context: Context): String = {

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
      val result: Future[Option[String]] = Future.sequence {
        logger.debug(s"Downloading files $fileLocations")
        fileLocations
          .map { location =>
            fileDownloader.downloadFromStore(location, new File(s"/tmp/${location.key}").toPath)
              .map { zippedFile =>
                val extractLocation = new File("/tmp")
                val zipFile = new ZipFile(zippedFile.toFile)
                zipFile.extractAll(extractLocation.getPath)
                extractLocation.toPath
              }.recover { case t: Throwable =>
              logger.error("Runtime error downloading file", t)
              throw t
            }
          }
      }.flatMap { _ =>
        val pageUpload: Future[FileUploadResult] = fileUploader.pushToStore(
          new File("/tmp/src/main/public/html/character.html").toPath,
          AwsFileLocation("vallonde", "src/main/public/html/character.html",
            Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read)))
        )
        val configUpload: Future[FileUploadResult] = {
          UploadServerlessHandler.buildConfigFile(
            new File("/tmp/vallonde-dev-stack-output.json"))
          fileUploader.pushToStore(
            new File("/tmp/config.js").toPath,
            AwsFileLocation("vallonde", "src/main/assets/js/config.js",
              Seq(GrantedPermission(GroupGrantee.AllUsers, Permission.Read)))
          )
        }
        val assetsUpload: Future[FileUploadResult] = fileUploader.pushToStore(
          new File("/tmp/src/main/assets/js/update_character.js").toPath,
          AwsFileLocation("vallonde-assets", "src/main/assets/js/update_character.js")
        )
        for {
          _ <- pageUpload
          _ <- configUpload
          _ <- assetsUpload
        } yield (): Unit
      }
        .map { _ =>
          codePipelineConnector.sendSuccessEvent(jobId, SuccessEventDetails(jobId))
          None
        }.recover {
        case s3: SdkClientException =>
          val message = "Runtime error when uploading files"
          logger.error(message, s3)
          codePipelineConnector.sendFailureEvent(jobId, FailureEventDetails(message))
          Some(message)
        case t: Throwable =>
          val message = "Error extracting file"
          logger.error(message, t)
          codePipelineConnector.sendFailureEvent(jobId, FailureEventDetails(message))
          Some(message)
      }
      Await.result(result, globalConfig.futureTimeout)
    }).fold(s"Error parsing JSON")(_.getOrElse(input))
  }
}

object UploadServerlessHandler {
  def buildConfigFile(input: File): Unit = {
    parse(scala.io.Source.fromFile(input).mkString)
      .toOption
      .flatMap(output => root.GetCharacterDataUrl.string.getOption(output))
      .map(x => Json.obj(
        "api-base-url" -> Json.fromString(x),
        "assets-uri" -> Json.fromString("https://s3.eu-west-2.amazonaws.com/vallonde-assets/src/main/assets/js/update_character.js")
      ))
      .map(x => s"var config = ${x.toString()};")
      .foreach { x =>
        val file = new File("/tmp/config.js")
        file.createNewFile()
        new FileOutputStream(file)
          .write(x.getBytes)
      }
  }
}
