# AWS Lambda Test Runner

[![Build Status](https://travis-ci.com/automatictester/lambda-test-runner.svg?branch=master)](https://travis-ci.com/automatictester/lambda-test-runner)

Run your unit tests with Maven on AWS Lambda!

## How it works

AWS Lambda Test Runner will:
- Install JDK on first run. Lambda ships with JRE, so it needs to install JDK first. In order to do so, it will run
  a shell command using Java's ProcessBuilder, which will download and extract TAR file with OpenJDK10 to the 
  writable location on Lambda filesystem: `/tmp`.
- Clone the Git repo you passed in your request using JGit, also to `/tmp`.
- Run another shell command, the one you passed in your request. There is no Maven available on Lambda, so your repo
  needs to contain [Maven Wrapper](https://github.com/takari/maven-wrapper) - wonderful tool which you
  want to use anyway. It's growing in popularity not without a reason. Note you need to include JVM argument
  `-Dmaven.repo.local=/tmp/.m2` in your command - this is because default **MAVEN_USER_HOME** `~/.m2`
  is not writable on Lambda.

## Limitations

As of November 2018, the key AWS Lambda Function [limits](https://docs.aws.amazon.com/lambda/latest/dg/limits.html) you'll be interested in are:
- Function memory allocation: up to 3008 MB.
- Function timeout: 900 seconds (15 minutes).
- `/tmp` directory storage: 512 MB.

If your tests need more time or memory to run, you won't be able to run them using AWS Lambda Test Runner. Pay special 
attention to 512 MB directory storage limit - it needs to accommodate unpacked JDK, your repo and local Maven cache 
in `.m2`.

Running tests with Gradle is not supported at this time due to disk space required by Gradle Wrapper.

I'd expect AWS to increase these limits in future. They have already increased function memory allocation limit and 
function timeout in the past. Adding more disk space or adding the ability to mount EFS has also been a common request 
among the AWS user community.

## Sample request

For sample request see sample payloads from end-to-end tests:
- [e2e/lambda-test-runner-payload.json](https://github.com/automatictester/lambda-test-runner/blob/master/e2e/lambda-test-runner-payload.json)
- [e2e/lightning-core-payload.json](https://github.com/automatictester/lambda-test-runner/blob/master/e2e/lightning-core-payload.json)
- [e2e/wiremock-maven-plugin-payload.json](https://github.com/automatictester/lambda-test-runner/blob/master/e2e/wiremock-maven-plugin-payload.json)

## Required environment variables

All Lambda configuration is managed through environment variables. See 
[tf/main.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/main.tf) for details. 
The variables you might want to customize:
- **BUILD_OUTPUTS** - S3 bucket for storing build outputs. You need to set this one to point to your bucket.
- **LOG_LEVEL** - you can switch between `info` and `debug`.
- **M2_CLEANUP** - if set to `true`, **MAVEN_USER_HOME** with local Maven cache will be purged 
  at the beginning of every execution to free up disk space.

No other environment variables are expected to be modified without a good reason.
