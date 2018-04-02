package components.upload_serverless_website.handlers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import components.upload_serverless_website.connectors.DefaultCodePipelineConnector
import config.global.GlobalConfig
import connectors.filestore.alpakka.AlpakkaFileStoreConnector
import connectors.filestore.amazon.{AmazonFileStoreConnector, S3ClientFactory}

import scala.concurrent.ExecutionContext

class DefaultUploadServerlessHandler extends RequestHandler[String, String] {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private implicit val system: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  private implicit val mat: ActorMaterializer = ActorMaterializer()

  override def handleRequest(input: String, context: Context): String = {
    new UploadServerlessHandler(
      new DefaultCodePipelineConnector(),
      new AlpakkaFileStoreConnector(),
      new AmazonFileStoreConnector(S3ClientFactory),
      GlobalConfig
    ).handleRequest(input, context)
  }
}
