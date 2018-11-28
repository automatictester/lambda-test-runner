# AWS Lambda Test Runner (experimental)

[![Build Status](https://travis-ci.com/automatictester/lambda-test-runner.svg?branch=master)](https://travis-ci.com/automatictester/lambda-test-runner)

Run your Java unit tests on AWS Lambda!  

## How it works

AWS Lambda Test Runner will:
- Install JDK on first run. Lambda ships with JRE, so we need to install JDK first. In order to do so, it will run
  a shell command using Java's ProcessBuilder, which will download and extract TAR file with OpenJDK10 to the only
  writable location on Lambda filesystem: `/tmp`.
- Clone the Git repo you passed in your request, also to `/tmp`.
- Run another shell command, the one you passed in your request. There is no Maven available on Lambda, so your repo
  needs to contain [Maven Wrapper](https://github.com/automatictester/lambda-test-runner) - wonderful tool which you
  want to use anyway. It's growing in popularity not without a reason. Note you need to include JVM argument
  `-Dmaven.repo.local=/tmp/.m2` in your command - this is because default Maven `.m2` location is not writable on
  Lambda.

## Limitations

The key AWS Lambda Function [limits](https://docs.aws.amazon.com/lambda/latest/dg/limits.html) you'll be interested in are:
- Function memory allocation: 128 MB to 3008 MB, in 64 MB increments.
- Function timeout: 900 seconds (15 minutes).
- `/tmp` directory storage: 512 MB.

If your tests need more time or memory to run, you won't be able to run them using AWS Lambda Test Runner. Pay special 
attention to 512 MB directory storage limit - it needs to accommodate JDK, your repo + local Maven cache in `.m2`. 


I'd expect AWS to increase these limits in future. They have already increased function memory allocation and function  
timeout in the past. Adding more disk space or adding the ability to mount EFS has also been a common request among
the AWS community.

## Sample request

```json
{
    "repoUri": "https://github.com/automatictester/lambda-test-runner.git",
    "command": "./mvnw test -Dtest=SmokeTest -Dmaven.repo.local=/tmp/.m2"
}
```

## Required environment variables

To be set in AWS Console:

```bash
MAVEN_USER_HOME=/tmp/.m2
JAVA_HOME=/tmp/jdk10
```

## TODOs

- End-to-end test
- Test concurrency
- Store results in S3
- Response class
- Add assertions to HandlerTest
- Log free space
- Test with Gradle Wrapper
- IAM role definition
- Make JDK version configurable
- Optional /tmp/.m2 cleanup
- Add JaCoCo
- Always verify exit code from ProcessRunner
- Checkout branch
- Private repo support
- Externally configurable log level
