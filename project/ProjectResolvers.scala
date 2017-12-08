import sbt._

object ProjectResolvers {

  def apply(): Seq[MavenRepository] = Seq(
    "Artima Maven Repository" at "http://repo.artima.com/releases",
    "DynamoDB Local Release Repository" at "https://s3.eu-central-1.amazonaws.com/dynamodb-local-frankfurt/release"
  )
}
