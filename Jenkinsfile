#!groovy

pipeline {
    agent {
        label 'linux'
    }
    options {
        timestamps()
        skipDefaultCheckout()
        disableConcurrentBuilds()
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
                sh './mvnw clean package'
            }
            post {
                always {
                    junit 'target/surefire-reports/junitreports/*.xml'
                    jacoco execPattern: '**/jacoco*.exec'
                }
            }
        }
        stage('Deploy to AWS') {
            steps {
                sh 'mv target/lambda-test-runner*.jar target/lambda-test-runner.jar'
                sh 'aws s3 cp target/lambda-test-runner.jar s3://automatictester.co.uk-lambda-test-runner-jar/lambda-test-runner.jar --region eu-west-2'
                dir('tf') {
                    sh 'terraform init -no-color'
                    sh 'terraform apply -no-color -auto-approve'
                }
            }
        }
        stage('End-to-end test') {
            steps {
                dir('e2e') {
                    sh './basic-test.sh'
                }
            }
        }
        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/lambda-test-runner.jar'
            }
        }
    }
}
