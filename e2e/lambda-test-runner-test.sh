#!/usr/bin/env bash

RESPONSE_JSON=lambda-test-runner-response.json
rm -f ${RESPONSE_JSON}

START=`date +%s`
aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --cli-read-timeout 0 --payload file://lambda-test-runner-payload.json ${RESPONSE_JSON}
END=`date +%s`
EXEC_TIME=$((END-START))

echo "Execution time: ${EXEC_TIME}s"

EXIT_CODE=`cat ${RESPONSE_JSON} | jq -r '.exitCode'`
OUTPUT=`cat ${RESPONSE_JSON} | jq -r '.output'`
S3_PREFIX=$(jq -r ".s3Prefix" ${RESPONSE_JSON})

aws s3 cp --exclude "*" --include "${S3_PREFIX}*" --recursive s3://automatictester.co.uk-lambda-test-runner-build-outputs . > /dev/null

if ! [[ $OUTPUT == *"Running uk.co.automatictester.lambdatestrunner.SmokeTest"* ]]; then
    echo "INCORRECT OUTPUT (1)"
    exit 1
fi

if ! [[ $OUTPUT == *"Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"* ]]; then
    echo "INCORRECT OUTPUT (2)"
    exit 1
fi

if ! [ $EXIT_CODE -eq 0 ]; then
    echo "INCORRECT EXIT CODE: $EXIT_CODE"
    exit 1
fi

if ! [ -f "${S3_PREFIX}/test-execution.log" ]; then
    echo "FILE WITH TEST EXECUTION LOG DOES NOT EXIST"
    exit 1
fi

if ! [ -f "${S3_PREFIX}/target/surefire-reports.zip" ]; then
    echo "ZIP FILE WITH BUILD OUTPUTS DOES NOT EXIST"
    exit 1
fi

rm -f ${RESPONSE_JSON}
