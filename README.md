# AWS Lambda Unit Test Runner (experimental)

[![Build Status](https://travis-ci.com/automatictester/lambda-test-runner.svg?branch=master)](https://travis-ci.com/automatictester/lambda-test-runner)

## Sample request

```json
{
    "command": "./mvnw test -Dtest=SmokeTest -Dmaven.repo.local=/tmp/.m2",
    "repoUri": "https://github.com/automatictester/lambda-test-runner.git"
}
```

## Required environment variables

To be set in AWS Console:

```bash
MAVEN_USER_HOME=/tmp/.m2
JAVA_HOME=/tmp/jdk10
```

## TODOs

- log free space
- optional /tmp/.m2 cleanup
- make JDK version configurable
- test concurrency
- add jacoco
- end-to-end test
- IAM role definition
- store results in S3
- always verify exit code from ProcessRunner
- checkout branch
- private repo support
- Response class
- add assertions to HandlerTest
