provider "aws" {
  region = "eu-west-2"
}

terraform {
  backend "s3" {
    bucket = "automatictester.co.uk-lambda-test-runner-tf-state"
    key = "lightning-lambda.tfstate"
    region = "eu-west-2"
  }
}

resource "aws_lambda_function" "lightning_ci" {
  function_name = "LambdaTestRunner"
  handler = "uk.co.automatictester.lambdatestrunner.Handler"
  runtime = "java8"
  s3_bucket = "automatictester.co.uk-lambda-test-runner-jar"
  s3_key = "lambda-test-runner.jar"
  source_code_hash = "${base64sha256(file("${path.module}/../target/lambda-test-runner.jar"))}"
  role = "arn:aws:iam::611654469811:role/LightningLambda"
  memory_size = "2048"
  timeout = "180"

  environment {
    variables = {
      GRADLE_CLEANUP = "false"            // for future use
      GRADLE_USER_HOME = "/tmp/.gradle"   // for future use
      JAVA_HOME = "/tmp/jdk10"
      LOG_LEVEL = "info"
      M2_CLEANUP = "false"
      MAVEN_USER_HOME = "/tmp/.m2"
      REPO_DIR = "/tmp/repo"
      TEMP_DIR = "/tmp"
    }
  }
}
