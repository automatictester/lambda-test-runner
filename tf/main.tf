provider "aws" {
  region               = "eu-west-2"
}

terraform {
  backend "s3" {
    bucket             = "automatictester.co.uk-lambda-test-runner-tf-state"
    key                = "lambda-test-runner.tfstate"
    region             = "eu-west-2"
  }
}

data "aws_caller_identity" "current" {}

resource "aws_iam_role" "lambda_test_runner_role" {
  name                 = "LambdaTestRunnerRole"
  description          = "Allows LambdaTestRunner function to use S3 and store logs in CloudWatch."
  assume_role_policy   = "${file("assume-role-policy.json")}"
}

resource "aws_s3_bucket" "jar" {
  bucket               = "automatictester.co.uk-lambda-test-runner-jar"
  acl                  = "private"
}

resource "aws_s3_bucket_object" "jar" {
  bucket               = "${aws_s3_bucket.jar.bucket}"
  key                  = "lambda-test-runner.jar"
  source               = "${path.module}/../target/lambda-test-runner.jar"
}

resource "aws_lambda_function" "lambda_test_runner" {
  function_name        = "LambdaTestRunner"
  handler              = "uk.co.automatictester.lambdatestrunner.Handler"
  runtime              = "java8"
  s3_bucket            = "${aws_s3_bucket.jar.bucket}"
  s3_key               = "${aws_s3_bucket_object.jar.key}"
  source_code_hash     = "${base64sha256(file("${path.module}/../target/lambda-test-runner.jar"))}"
  role                 = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.lambda_test_runner_role.name}"
  memory_size          = "2048"
  timeout              = "180"

  environment {
    variables = {
      GRADLE_CLEANUP   = "false"          // for future use
      GRADLE_USER_HOME = "/tmp/.gradle"   // for future use
      JAVA_HOME        = "/tmp/jdk10"
      LOG_LEVEL        = "info"
      M2_CLEANUP       = "false"
      MAVEN_USER_HOME  = "/tmp/.m2"
      REPO_DIR         = "/tmp/repo"
      TEMP_DIR         = "/tmp"
    }
  }
}

resource "aws_iam_role_policy_attachment" "s3-access-policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "cloudwatch-access-policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}
