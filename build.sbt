lazy val root = Project(APP_NAME, file("."))
  .enablePlugins(SbtTwirl)
  .configs(IntegrationTest)
  .settings(
    name := APP_NAME,
    version := VERSION,
    scalaVersion := SCALA_VERSION
  )
  .settings(
    resolvers ++= ProjectResolvers()
  )
  .settings(
    libraryDependencies ++= ProjectDependencies()
  )
  .settings(
    Defaults.itSettings
  )

val APP_NAME = "vallonde"
val VERSION = "999-SNAPSHOT"
val SCALA_VERSION = "2.12.4"
