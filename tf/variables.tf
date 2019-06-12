variable "s3_bucket_jar" {
  default = "automatictester.co.uk-lambda-test-runner-jar"
}
variable "s3_bucket_build_outputs" {
  default = "automatictester.co.uk-lambda-test-runner-build-outputs"
}
variable "s3_bucket_ssh_keys" {
  default = "automatictester.co.uk-ssh-keys"
}
variable "s3_bucket_object_ssh_key_key" {
  default = "id_rsa_lambda_test_runner"
}
variable "jar_file_name" {
  default = "lambda-test-runner.jar"
}
variable "java_version" {
  default = "10.0.2"
}
