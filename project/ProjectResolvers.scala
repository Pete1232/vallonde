import sbt._

object ProjectResolvers {

  def apply(): Seq[MavenRepository] = Seq(
    "Artima Maven Repository" at "http://repo.artima.com/releases"
  )
}
