# AWS Lambda Test Runner

[![Build Status](https://travis-ci.com/automatictester/lambda-test-runner.svg?branch=master)](https://travis-ci.com/automatictester/lambda-test-runner)

Run your unit tests with Maven directly on AWS Lambda.

## Project history

It all started one day when I was updating my [Packer](https://www.packer.io/) templates for building my Jenkins EC2 slaves. 
I asked myself: "Hold on, why am I doing this? Isn't running unit tests a perfect use case for AWS Lambda?". I think it is, and this is 
how it all began - I have started this open source project as an experiment aiming to explore how much AWS Lambda serverless technology 
can help us test our software.

## Project goal

To provide an efficient test runner based on AWS Lambda technology for the Java ecosystem.

## Project aspirations

- Stay as decoupled as possible from any particular Git hosting.
- Provide the runner, not the orchestration - if your current CI server is Jenkins, AWS Lambda Test Runner may help you reduce your dependency 
  on Jenkins slaves, not on Jenkins master. Of course you don't have to be using Jenkins to use AWS Lambda Test Runner.

## Supported platforms

### Build tools

- [x] Maven - requires [Maven Wrapper](https://github.com/takari/maven-wrapper)
- [ ] Gradle - currently not supported due to disk space required by Gradle Wrapper
- [ ] Other Java/JVM build tools - not verified

### Java version

- [x] OpenJDK10 - is used to run your tests

## How it works

AWS Lambda Test Runner will:
- Install JDK on first run. AWS Lambda ships with JRE, so it needs to install JDK first. In order to do so, it will run a shell command using Java's ProcessBuilder,
  which will download and extract TAR file with JDK to the writable location on Lambda filesystem: `/tmp`.
- Clone the Git repo you passed in your request using JGit, also to `/tmp`.
- Run another shell command, the one you passed in your request. There is no Maven installed on Lambda, so your repo needs to contain Maven Wrapper - wonderful tool 
  which you want to use anyway. It's growing in popularity not without a reason. Note you need to include JVM argument `-Dmaven.repo.local=/tmp/.m2`
  in your command - this is because default **MAVEN_USER_HOME** `~/.m2` is not writable on Lambda.

## How to deploy it

- Clone the repo.
- Generate Java JAR: `./mvnw clean package`.
- Deploy it to your AWS account. There is a [Terraform script](https://github.com/automatictester/lambda-test-runner/blob/master/tf/main.tf) that should help.
  Before you go ahead, customize the variables at the top of that file, plus terraform backend bucket.
- Don't forget to check [Required environment variables](https://github.com/automatictester/lambda-test-runner#required-environment-variables).

## Limitations

Usual AWS Lambda service limits apply. As of November 2018, the key [limits](https://docs.aws.amazon.com/lambda/latest/dg/limits.html) you'll be interested in are:
- Function memory allocation: up to 3008 MB.
- Function timeout: 900 seconds (15 minutes).
- `/tmp` directory storage: 512 MB.

If your tests need more time or memory to run, you won't be able to run them using AWS Lambda Test Runner. Pay special attention to 512 MB directory 
storage limit - it needs to accommodate unpacked JDK, your repo and local Maven cache in `.m2`.

I expect AWS to increase these limits in future. They have already increased function memory allocation limit and function timeout in the past. 
Adding more disk space or adding the ability to mount EFS has also been a common request among the AWS user community.

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
- **SSH_KEY_BUCKET** and **SSH_KEY_KEY** - your S3 bucket and object key with private SSH key (see [SSH access](https://github.com/automatictester/lambda-test-runner#SSH-access)).

No other environment variables are expected to be modified without a good reason.

## SSH access

To clone public repos, you should provide HTTPS URL in your request payload. If you intend to clone only public repos, you can ignore remainder of this section.

To clone private repos, you should provide SSH URL in your request payload, as well as configure a few other things:
- Set **SSH_KEY_BUCKET** and **SSH_KEY_KEY** environment variables (see [Required environment variables](https://github.com/automatictester/lambda-test-runner#required-environment-variables))
  to point at the SSH key you want to use.
- The SSH key you use should be compliant with both JGit and the Git hosting provider you are using. To generate JGit-, GitHub- and BitBucket-compilant SSH key,
  you can use this command: `ssh-keygen -m PEM -t rsa -b 4096`
