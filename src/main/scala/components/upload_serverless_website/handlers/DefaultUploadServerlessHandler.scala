package components.upload_serverless_website.handlers

import java.io._
import java.util.stream.Collectors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import components.upload_serverless_website.connectors.DefaultCodePipelineConnector
import config.global.GlobalConfig
import connectors.filestore.alpakka.AlpakkaFileStoreConnector
import connectors.filestore.amazon.{AmazonFileStoreConnector, S3ClientFactory}

import scala.concurrent.ExecutionContext

class DefaultUploadServerlessHandler extends RequestStreamHandler {

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private implicit val system: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  private implicit val mat: ActorMaterializer = ActorMaterializer()

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {

    val parsedInput: String = new BufferedReader(new InputStreamReader(input))
      .lines()
      .collect(Collectors.joining("\n"))

    val result: String = new UploadServerlessHandler(
      new DefaultCodePipelineConnector(),
      new AlpakkaFileStoreConnector(),
      new AmazonFileStoreConnector(S3ClientFactory),
      GlobalConfig
    ).handleRequest(parsedInput, context)

    new BufferedWriter(new OutputStreamWriter(output))
      .write(result)
  }
}
