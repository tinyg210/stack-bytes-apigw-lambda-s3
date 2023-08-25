#!/bin/sh

for i in {1..50}
do
  FUNCTION_NAME="lambda-function-$i"

  aws --endpoint "http://localhost:4566" lambda invoke --function-name $FUNCTION_NAME \
                                                --cli-binary-format raw-in-base64-out \
                                                --payload '{"body": "'$i'"}' output.txt

  echo "Invoked Lambda function: $FUNCTION_NAME"
done