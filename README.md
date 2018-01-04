## Running the tests
The integration tests are designed to run against a [localstack](https://github.com/localstack/localstack|localstack)
instance - in particular one running in docker(though any proxy server should work).
The config can be found in the `it/resources/application.conf` file.

Also note that the host of the proxy server is set by the `AWS_PROXY_HOST` environment variable.
This is to help with inconsistencies with incompatibilities between different operating systems using localstack.
It defaults to `localhost` which should work for a docker instance running on Linux.
