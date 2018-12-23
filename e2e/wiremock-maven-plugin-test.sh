#!/usr/bin/env bash

RESPONSE_JSON=wiremock-maven-plugin-response.json
rm -f ${RESPONSE_JSON}

START=`date +%s`
aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --cli-read-timeout 0 --payload file://wiremock-maven-plugin-payload.json ${RESPONSE_JSON}
END=`date +%s`
EXEC_TIME=$((END-START))

echo "Execution time: ${EXEC_TIME}s"

EXIT_CODE=`cat ${RESPONSE_JSON} | jq -r '.exitCode'`
OUTPUT=`cat ${RESPONSE_JSON} | jq -r '.output'`

if ! [[ $OUTPUT == *"Failures: 0, Errors: 0, Skipped: 0"* ]]; then
    echo "INCORRECT OUTPUT (1)"
    exit 1
fi

if ! [[ $OUTPUT == *"BUILD SUCCESS"* ]]; then
    echo "INCORRECT OUTPUT (2)"
    exit 1
fi

if ! [ $EXIT_CODE -eq 0 ];then
    echo "INCORRECT EXIT CODE: $EXIT_CODE"
    exit 1
fi
