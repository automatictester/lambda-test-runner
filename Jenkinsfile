#!groovy

pipeline {
    agent {
        label 'linux'
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
                    git credentialsId: 'github-creds', url: 'git@github.com:automatictester/lambda-test-runner.git'
                }
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
                    sh './lightning-core-test.sh'
                    sh './sample-sbt-project-test.sh'
                }
            }
        }
    }
}
