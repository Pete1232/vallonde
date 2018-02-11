import ProjectConfigs._

lazy val root = Project(APP_NAME, file("."))
  .enablePlugins(SbtTwirl)
  .configs(IntegrationTest, LambdaTest, LambdaTestClean)
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
    inConfig(LambdaTest)(
      Defaults.testSettings ++ Seq(
        parallelExecution := false
      )
    ): _*
  )
  .settings(
    (test in LambdaTestClean) := ((test in LambdaTest) dependsOn assembly).value
  )
  .settings(
    Defaults.itSettings
  )
  .settings(
    AssemblyStrategy(),
    test in assembly := {}
  )

val APP_NAME = "vallonde"
val VERSION = "999-SNAPSHOT"
val SCALA_VERSION = "2.12.4"
