# AWS Lambda Test Runner (experimental)

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
- Function memory allocation: 128 MB to 3008 MB, in 64 MB increments.
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

For sample request see [e2e/payload.json](https://github.com/automatictester/lambda-test-runner/blob/master/e2e/payload.json).

## Required environment variables

For sample request see [tf/main.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/main.tf).

## TODOs

- Test concurrency
- Store results in S3
- IAM role definition
- Make JDK version configurable
- Private repo support
- Limit output size
- Document configurability
