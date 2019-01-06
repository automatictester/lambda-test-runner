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

resource "aws_s3_bucket" "jar" {
  bucket               = "${var.s3_bucket_jar}"
  acl                  = "private"
}

resource "aws_s3_bucket_object" "jar" {
  bucket               = "${aws_s3_bucket.jar.bucket}"
  key                  = "${var.jar_file_name}"
  source               = "${path.module}/../target/${var.jar_file_name}"
  etag                 = "${md5(file("${path.module}/../target/${var.jar_file_name}"))}"
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

resource "aws_iam_role" "lambda_test_runner_role" {
  name                 = "LambdaTestRunnerRole"
  description          = "Allow LambdaTestRunner to use S3 and CloudWatch Logs."
  assume_role_policy   = "${file("iam-policy/assume-role-policy.json")}"
}

resource "aws_iam_policy" "s3_put_build_outputs" {
  name                 = "LambdaTestRunnerPutBuildOutputsToS3"
  path                 = "/"
  description          = "Put LambdaTestRunner Build Outputs to S3"
  policy               = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "s3:PutObject",
            "Effect": "Allow",
            "Resource": "arn:aws:s3:::${aws_s3_bucket.build_outputs.bucket}/*"
        }
    ]
}
EOF
}

resource "aws_iam_policy" "s3_get_ssh_key" {
  name                 = "LambdaTestRunnerGetSshKeyFromS3"
  path                 = "/"
  description          = "LambdaTestRunner Get Ssh Key From S3"
  policy               = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "s3:GetObject",
            "Effect": "Allow",
            "Resource": "arn:aws:s3:::${var.s3_bucket_ssh_keys}/${var.s3_bucket_object_ssh_key_key}"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "s3_build_outputs_access_policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "${aws_iam_policy.s3_put_build_outputs.arn}"
}

resource "aws_iam_role_policy_attachment" "s3_ssh_key_access_policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "${aws_iam_policy.s3_get_ssh_key.arn}"
}

resource "aws_iam_role_policy_attachment" "cloudwatch_access_policy" {
  role                 = "${aws_iam_role.lambda_test_runner_role.name}"
  policy_arn           = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "lambda_test_runner" {
  function_name                  = "LambdaTestRunner"
  handler                        = "uk.co.automatictester.lambdatestrunner.Handler"
  runtime                        = "java8"
  s3_bucket                      = "${aws_s3_bucket.jar.bucket}"
  s3_key                         = "${aws_s3_bucket_object.jar.key}"
  source_code_hash               = "${base64sha256(file("${path.module}/../target/${var.jar_file_name}"))}"
  role                           = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${aws_iam_role.lambda_test_runner_role.name}"
  memory_size                    = "2048"
  timeout                        = "180"
  reserved_concurrent_executions = "1"

  environment {
    variables = {
      BUILD_OUTPUTS         = "${aws_s3_bucket.build_outputs.bucket}"
      JAVA_HOME             = "/tmp/jdk10"
      LOG_LEVEL             = "info"
      M2_CLEANUP            = "true"
      MAVEN_USER_HOME       = "/tmp/.m2"
      REPO_DIR              = "/tmp/repo"
      SBT_CLEANUP           = "true"
      SBT_GLOBAL_BASE       = "/tmp/.sbt"
      SBT_IVY_HOME          = "/tmp/.ivy2"
      SSH_KEY_BUCKET        = "${var.s3_bucket_ssh_keys}"
      SSH_KEY_KEY           = "${var.s3_bucket_object_ssh_key_key}"
      SSH_KEY_LOCAL         = "/tmp/id_rsa"
      TEMP_DIR              = "/tmp"
      TEST_EXECUTION_LOG    = "test-execution.log"
    }
  }
}
