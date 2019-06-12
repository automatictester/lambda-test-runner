# AWS Lambda Test Runner

[![Build Status](https://travis-ci.com/automatictester/lambda-test-runner.svg?branch=master)](https://travis-ci.com/automatictester/lambda-test-runner) [![Join the chat at https://gitter.im/automatictester/lambda-test-runner](https://badges.gitter.im/automatictester/lambda-test-runner.svg)](https://gitter.im/automatictester/lambda-test-runner?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Run your unit tests with Maven or SBT directly on AWS Lambda. 
For related Jenkins plugin see [here](https://github.com/jenkinsci/lambda-test-runner-plugin).

## Project history

It all started one day when I was updating my [Packer](https://www.packer.io/) templates for building my Jenkins EC2 slaves. 
I asked myself: *"Hold on, why am I doing this? Isn't running unit tests a perfect use case for AWS Lambda?"*. I think it is, and this is 
how it all began - I have started this open source project as an experiment aiming to explore how much AWS Lambda serverless technology 
can help us test our software.

## Project goal

To provide an efficient test runner based on AWS Lambda technology for the Java ecosystem.

## Project aspirations

- Provide support for multiple Git hostings.
- Provide support for multiple build tools.
- Provide the runner, not the orchestration. In other words, if your current CI server is Jenkins, AWS Lambda Test Runner may help you reduce your dependency 
  on Jenkins slaves, not on Jenkins master. Of course you don't have to be using Jenkins to use AWS Lambda Test Runner.

## Benefits

- Up to 1000 concurrent executions.
- No maintenance of build agents.
- You don't pay for what you don't use.

## Supported platforms

### Git hosting

- [x] GitHub - HTTPS and SSH
- [x] BitBucket - HTTPS and SSH
- [x] GitLab - HTTPS and SSH
- [ ] Other - not verified

### Build tools

- [x] Maven - requires [Maven Wrapper](https://github.com/takari/maven-wrapper)
- [x] SBT - requires core SBT binaries to be included in your repo
- [ ] Gradle - currently not supported due to required disk space
- [ ] Other - not verified

### Java version

Java version used to run the tests:

- [x] OpenJDK 9.0.4
- [x] OpenJDK 10.0.2 (default)
- [x] OpenJDK 11.0.2
- [x] OpenJDK 12.0.1
- [ ] Other - not supported

## How it works

AWS Lambda Test Runner will:
- Install JDK to `/tmp` on first run, as Java runtime for AWS Lambda doesn't ship with `javac`.
- Get SSH key from S3, if Git repository URI is for cloning with SSH.
- Clone Git repository you passed in your request to `/tmp` using JGit library.
- Run shell command which you passed in your request. There are no build tools available on Lambda, so it needs to be included in your repo.
  See [Build tools](https://github.com/automatictester/lambda-test-runner#build-tools) and 
  [Build tool examples](https://github.com/automatictester/lambda-test-runner#build-tool-examples) for more information.
- Store build outputs to S3, if requested. Test execution log is always stored to S3.
- Log events to CloudWatch Logs.

Sample execution log from CloudWatch Logs:

<img src="img/aws-lambda-test-runner-execution-log.png" width="923" height="420"/>

## Architecture

This is the big picture:

<img src="img/aws-lambda-test-runner-architecture.png" width="738" height="419"/>

## How to deploy it

- You can either start by downloading the JAR from [Releases](https://github.com/automatictester/lambda-test-runner/releases)
  tab, or building it yourself. If you decide to go for the latter, clone the repo and build Java JAR file with: `./mvnw clean package -DskipTests`.
- Deploy it to your AWS account. There is [Terraform](https://github.com/automatictester/lambda-test-runner/blob/master/tf) scripts to speed things up.
  Before running `terraform apply`, you will need to: 
  - Customize all variables in [tf/variables.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/variables.tf). 
  - Customize Terraform backend S3 bucket in [tf/main.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/main.tf).
- Don't forget to check [Required environment variables](https://github.com/automatictester/lambda-test-runner#required-environment-variables).
- If you plan to clone Git repositories over SSH, create private S3 bucket and upload private SSH key as a private S3 object. Bucket name and object key
  have to match variables `s3_bucket_ssh_keys` and `s3_bucket_object_ssh_key_key` defined in 
  [tf/variables.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/variables.tf).

## Required environment variables

All Lambda configuration is managed through environment variables. See 
[tf/main.tf](https://github.com/automatictester/lambda-test-runner/blob/master/tf/main.tf) for details.

Variables you may want to customize:
- `BUILD_OUTPUTS` - S3 bucket for storing build outputs. If you followed [How to deploy it](https://github.com/automatictester/lambda-test-runner#how-to-deploy-it)
  it should be already customized.
- `JAVA_VERSION` - Java version used to run the tests. Options: `9.0.4`, `10.0.2` (default), `11.0.2`, `12.0.1`.
- `LOG_LEVEL` - you can switch between `info` and `debug`.
- `M2_CLEANUP` - if set to `true`, `MAVEN_USER_HOME` will be purged at the beginning of every execution to free up disk space.
- `SBT_CLEANUP` - if set to `true`, `SBT_GLOBAL_BASE` and `SBT_IVY_HOME` will be purged at the beginning of every execution to free up disk space.
- `SSH_KEY_BUCKET` and `SSH_KEY_KEY` - your S3 bucket and object key with private SSH key 
  (see [SSH access](https://github.com/automatictester/lambda-test-runner#SSH-access)).
  If you followed [How to deploy it](https://github.com/automatictester/lambda-test-runner#how-to-deploy-it), both should be already customized.

No other environment variables are expected to be modified without a good reason.

## Usage example

AWS Lambda Test Runner is an AWS Lambda function and can be invoked like other Lambdas. To make it easier for Jenkins users, there is a 
[Jenkins Plugin for AWS Lambda Test Runner](https://github.com/jenkinsci/lambda-test-runner-plugin) which you can use to trigger the execution. 
If you do not use Jenkins, or prefer to orchestrate the execution yourself, below is a step-by-step guide to executing already deployed AWS Lambda Test Runner 
using `aws cli`. It requires all necessary tools to be installed and configured.

Below is sample JSON payload: 

```
cat wiremock-maven-plugin-payload.json 
{
  "repoUri": "https://github.com/automatictester/wiremock-maven-plugin.git",
  "branch": "master",
  "command": "./mvnw test -Dmaven.repo.local=${MAVEN_USER_HOME}",
  "storeToS3" : ["target/surefire-reports"]
}
```

This payload tells AWS Lambda Test Runner which Git repo to clone, which branch to check out, how to run the tests and which build outputs you want to store to S3.

Now we will use this payload to invoke Lambda function:

```
aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --cli-read-timeout 0 \
  --payload file://wiremock-maven-plugin-payload.json wiremock-maven-plugin-response.json
```

It is critical to override the default maximum socket read time with `--cli-read-timeout`. If we don't do that and our tests take more than 60s to execute, 
Lambda will automatically trigger a retry with all its consequences. This is not the behaviour we want.

This assumes your Lambda is named `LambdaTestRunner` and was deployed to `eu-west-2`. The JSON response will be stored to `wiremock-maven-plugin-response.json`. 

Now you can inspect content of the `wiremock-maven-plugin-response.json` file:

```
cat wiremock-maven-plugin-response.json
```

It should look similar to this one:

```
{
  "output": "...",
  "exitCode": 0,
  "s3Prefix": "2018-12-11-13-33-10",
  "requestId": "418eaf5d-fd49-11e8-8fd7-ade5a41cf0d6"
}
```

We can now read `s3Prefix` into `S3_PREFIX` variable, which we will use in a subsequent command:

```
S3_PREFIX=$(jq -r ".s3Prefix" wiremock-maven-plugin-response.json)
```

Now we can fetch from S3 the build outputs. You will need to substitute the S3 bucket I use in the example below 
(`automatictester.co.uk-lambda-test-runner-build-outputs`) with your own S3 bucket name - see 
[How to deploy it](https://github.com/automatictester/lambda-test-runner#how-to-deploy-it) for details:

```
aws s3 cp --exclude "*" --include "${S3_PREFIX}*" --recursive \
  s3://automatictester.co.uk-lambda-test-runner-build-outputs .
```

At this point we have the build outputs on the local file system. This will include:
- Test execution log in `test-execution.log`.
- ZIP files with the directories we requested to store to S3, in this case `target/surefire-reports.zip`.

They can be now processed in the usual way.

## Request parameters

Below is an example of an invokation request with all supported parameters:

```
{
  "repoUri": "https://github.com/automatictester/lambda-test-runner.git",
  "branch": "master",
  "command": "./mvnw test -Dtest=SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}",
  "storeToS3" : ["target/surefire-reports", "target/surefire-reports"]
}
```

Parameters:
- `repoUri`: (Required) URI of Git repo to clone. Both HTTPS and SSH clones are supported.
- `command`: (Required) Command to run the tests. See [Build tool examples](https://github.com/automatictester/lambda-test-runner#build-tool-examples) for more information.
- `branch`: (Optional) Git branch. If not specified, defaults to `HEAD`.
- `storeToS3`: (Optional) list of directories to store to S3. Valid values include: `["target/surefire-reports", "target/failsafe-reports"]`,
`["target/surefire-reports"]`, `[]`.

## Build tool examples

Sample request payload for running Maven tests:

``` 
{
  ...
  "command": "./mvnw test -Dmaven.repo.local=${MAVEN_USER_HOME}",
  ...
}
```

Sample request payload for running SBT tests:

``` 
{
  ...
  "command": "./sbt -Dsbt.global.base=${SBT_GLOBAL_BASE} -Dsbt.ivy.home=${SBT_IVY_HOME} test",
  ...
}
```

JVM options defined in the above examples are necessary due to locations other than `/tmp` not being writable on AWS Lambda.

## Git repository SSH access

To clone public repos, you should provide HTTPS URL in your request payload. If you intend to clone only public repos, you can ignore remainder of this section.

To clone private repos, you should provide SSH URL in your request payload, as well as configure a few other things:
- Set `SSH_KEY_BUCKET` and `SSH_KEY_KEY` environment variables 
  (see [Required environment variables](https://github.com/automatictester/lambda-test-runner#required-environment-variables))
  to point at the SSH key you want to use.
- The SSH key you use should be compliant with both JGit and the Git hosting you are using. To generate such SSH key, you can use this command:
  `ssh-keygen -m PEM -t rsa -b 4096`

## Limitations

Usual AWS Lambda service limits apply. As of November 2018, the key [limits](https://docs.aws.amazon.com/lambda/latest/dg/limits.html) you'll be interested in are:
- Function memory allocation: up to 3008 MB.
- Function timeout: 900 seconds (15 minutes).
- `/tmp` directory storage: 512 MB.

If your tests need more time or memory to run, you won't be able to run them using AWS Lambda Test Runner. Pay special attention to 512 MB directory 
storage limit - it needs to accommodate unpacked JDK, your repo and local Maven cache in `.m2`.

I expect AWS to increase these limits in future. They have already increased function memory allocation limit and function timeout in the past. 
Adding more disk space or adding the ability to mount EFS has also been a common request among the AWS user community.
