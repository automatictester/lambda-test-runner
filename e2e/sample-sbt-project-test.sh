#!/usr/bin/env bash

rm -f sample-sbt-project-response.json

START=`date +%s`
aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --cli-read-timeout 0 --payload file://sample-sbt-project-payload.json sample-sbt-project-response.json
END=`date +%s`
EXEC_TIME=$((END-START))

echo "Execution time: ${EXEC_TIME}s"

EXIT_CODE=`cat sample-sbt-project-response.json | jq -r '.exitCode'`
OUTPUT=`cat sample-sbt-project-response.json | jq -r '.output'`
S3_PREFIX=$(jq -r ".s3Prefix" lambda-test-runner-response.json)

aws s3 cp --exclude "*" --include "${S3_PREFIX}*" --recursive s3://automatictester.co.uk-lambda-test-runner-build-outputs . > /dev/null

if ! [[ $OUTPUT == *"Tests: succeeded 2"* ]]; then
    echo "INCORRECT OUTPUT (1)"
    exit 1
fi

if ! [[ $OUTPUT == *"All tests passed"* ]]; then
    echo "INCORRECT OUTPUT (2)"
    exit 1
fi

if ! [ $EXIT_CODE -eq 0 ];then
    echo "INCORRECT EXIT CODE: $EXIT_CODE"
    exit 1
fi

if ! [ -f "${S3_PREFIX}/test-execution.log" ]; then
    echo "FILE WITH TEST EXECUTION LOG DOES NOT EXIST"
    exit 1
fi

if ! [ -f "${S3_PREFIX}/jdk-installation.log" ]; then
    echo "FILE WITH JDK INSTALLATION LOG DOES NOT EXIST"
    exit 1
fi
