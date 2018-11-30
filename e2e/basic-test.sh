#!/usr/bin/env bash

aws lambda invoke --function-name LambdaTestRunner --region eu-west-2 --payload file://payload.json response.json
