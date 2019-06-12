#!groovy

pipeline {
    agent {
        label 'linux'
    }
    parameters {
        string(name: 'RELEASE_VERSION', defaultValue: '0.9.0', description: '')
        string(name: 'POST_RELEASE_SNAPSHOT_VERSION', defaultValue: '0.9.1-SNAPSHOT', description: '')
        booleanParam(name: 'RELEASE', defaultValue: false, description: '')
    }
    options {
        timestamps()
        skipDefaultCheckout()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Cleanup') {
            steps {
                cleanWs()
            }
        }
        stage('Clone') {
            steps {
                sshagent(['github-creds']) {
                    git branch: "${env.BRANCH_NAME}", credentialsId: 'github-creds', url: 'git@github.com:automatictester/lambda-test-runner.git'
                }
            }
        }
        stage('Set release version number') {
            steps {
                sh "./mvnw versions:set -DnewVersion=${params.RELEASE_VERSION}"
                sh "git add -A; git commit -m 'Release version bump'"
            }
        }
        stage('Test') {
            steps {
                sh './mvnw clean package -P jenkins'
            }
            post {
                always {
                    junit 'target/*-reports/junitreports/*.xml'
                }
            }
        }
        stage('Deploy to AWS') {
            steps {
                dir('tf') {
                    sh 'terraform init -no-color'
                    sh 'terraform apply -no-color -auto-approve'
                }
            }
        }
        stage('End-to-end test') {
            steps {
                dir('e2e') {
                    sh './lambda-test-runner-test.sh'
                    sh './wiremock-maven-plugin-test.sh'
                    sh './sample-sbt-project-test.sh'
                    sh './lightning-core-test.sh'
                }
            }
        }
        stage('Test Java versions') {
            steps {
                dir('tf') {
                    sh 'terraform apply -no-color -auto-approve -var-file="jdk-9.tfvars"'
                }
                dir('e2e') {
                    sh './lambda-test-runner-test.sh'
                }
                dir('tf') {
                    sh 'terraform apply -no-color -auto-approve -var-file="jdk-10.tfvars"'
                }
                dir('e2e') {
                    sh './lambda-test-runner-test.sh'
                }
                dir('tf') {
                    sh 'terraform apply -no-color -auto-approve -var-file="jdk-11.tfvars"'
                }
                dir('e2e') {
                    sh './lambda-test-runner-test.sh'
                }
                dir('tf') {
                    sh 'terraform apply -no-color -auto-approve -var-file="jdk-12.tfvars"'
                }
                dir('e2e') {
                    sh './lambda-test-runner-test.sh'
                }
            }
        }
        stage('Tag release') {
            steps {
                sh "git tag ${params.RELEASE_VERSION}"
            }
        }
        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/lambda-test-runner.jar'
            }
        }
        stage('Set snapshot version number') {
            steps {
                sh "./mvnw versions:set -DnewVersion=${params.POST_RELEASE_SNAPSHOT_VERSION}"
                sh "git add -A; git commit -m 'Post-release version bump'"
            }
        }
        stage('Push to origin') {
            when {
                expression {
                    "${params.RELEASE}".toBoolean() && "${env.BRANCH_NAME}" == "master"
                }
            }
            steps {
                sshagent(['github-creds']) {
                    sh 'git push --set-upstream origin master; git push --tags'
                }
            }
        }
    }
}
