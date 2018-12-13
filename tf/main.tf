terraform {
  backend "s3" {
    bucket             = "automatictester.co.uk-lambda-test-runner-tf-state"
    key                = "lambda-test-runner.tfstate"
    region             = "eu-west-2"
  }
}

provider "aws" {
  region               = "eu-west-2"
}

resource "aws_iam_role" "lambda_test_runner_role" {
  name                 = "LambdaTestRunnerRole"
  description          = "Allows LambdaTestRunner function to use S3 and store logs in CloudWatch."
  assume_role_policy   = "${file("assume-role-policy.json")}"
}

resource "aws_s3_bucket" "jar" {
  bucket               = "${var.s3_bucket_jar}"
  acl                  = "private"
}

resource "aws_s3_bucket_object" "jar" {
  bucket               = "${var.s3_bucket_jar}"
  key                  = "lambda-test-runner.jar"
  source               = "${path.module}/../target/lambda-test-runner.jar"
  etag                 = "${md5(file("${path.module}/../target/lambda-test-runner.jar"))}"
}

resource "aws_s3_bucket" "build_outputs" {
  bucket               = "${var.s3_bucket_build_outputs}"
  acl                  = "private"
  force_destroy        = true
  lifecycle_rule {
    id = "Delete all objects after 1 day"
    enabled = true
    expiration {
      days = 1
    }
  }
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
      BUILD_OUTPUTS    = "${var.s3_bucket_build_outputs}"
      JAVA_HOME        = "/tmp/jdk10"
      LOG_LEVEL        = "info"
      M2_CLEANUP       = "false"
      MAVEN_USER_HOME  = "/tmp/.m2"
      REPO_DIR         = "/tmp/repo"
      SBT_CLEANUP      = "false"
      SBT_GLOBAL_BASE  = "/tmp/.sbt"
      SBT_IVY_HOME     = "/tmp/.ivy2"
      SSH_KEY_BUCKET   = "${var.s3_bucket_ssh_keys}"
      SSH_KEY_KEY      = "id_rsa_lambda_test_runner"
      SSH_KEY_LOCAL    = "/tmp/id_rsa"
      TEMP_DIR         = "/tmp"
    }
  }
}

resource "aws_iam_role_policy_attachment" "s3_access_policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "cloudwatch_access_policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}
