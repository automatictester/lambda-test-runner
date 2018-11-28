#!groovy

multibranchPipelineJob('lambda-test-runner') {
    branchSources {
        git {
            remote('git@github.com:automatictester/lambda-test-runner.git')
            credentialsId('github-creds')
        }
    }
}
