#!/usr/bin/env bash

rm -f lightning-core-response.json

START=`date +%s`
aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --cli-read-timeout 0 --payload file://lightning-core-payload.json lightning-core-response.json
END=`date +%s`
EXEC_TIME=$((END-START))

echo "Execution time: ${EXEC_TIME}s"

EXIT_CODE=`cat lightning-core-response.json | jq -r '.exitCode'`
OUTPUT=`cat lightning-core-response.json | jq -r '.output'`

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
