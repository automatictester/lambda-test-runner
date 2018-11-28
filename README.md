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
