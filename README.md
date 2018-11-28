# AWS Lambda Unit Test Runner (experimental)

## Sample request

```json
{
    "targetDir": "/tmp/lambda-test-runner/",
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

- general code cleanup
- extract /tmp and subdirs from all classes
- log free space
- optional /tmp/.m2 cleanup
- make JDK version configurable
- test concurrency
- /bin/bash
- add jacoco
- end-to-end test
- add travis for building PRs
- IAM role definition
- store results in S3
- always verify exit code from ProcessRunner
- checkout branch
- private repo support
- Response class
- add assertions to HandlerTest
