## Running the tests
The integration tests are designed to run against a [localstack](https://github.com/localstack/localstack|localstack)
instance - in particular one running in docker(though any proxy server should work).
The config can be found in the `it/resources/application.conf` file.

Also note that the host of the proxy server is set by the `AWS_PROXY_HOST` environment variable.
This is to help with inconsistencies with incompatibilities between different operating systems using localstack.
It defaults to `localhost` which should work for a docker instance running on Linux.

## Testing in the build environment
The application is built by AWS CodeDeploy in the [hseeberger/scala-sbt](https://github.com/hseeberger/scala-sbt) docker image.

To replicate the build environment start up the container and run the commands in buildspec.yaml
(don't forget to export the environment variables).

Once that's done clone down this repo into the container and run all the tests.

#### Running tests against lambda
The integration test pack includes tests that can be run against Lambda functions.
However, since creating a Lambda function depends on a build package being created and uploaded these have been placed in a different test scope.

Run the tests for lambda with `sbt lt:test`
