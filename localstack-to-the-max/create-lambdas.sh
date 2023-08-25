#!/bin/sh

for i in {1..50}
do
  FUNCTION_NAME="lambda-function-$i"

  awslocal lambda create-function \
    --function-name $FUNCTION_NAME \
    --runtime java17 \
    --handler lambda.LambdaFunction::handleRequest \
    --memory-size 128 \
    --zip-file fileb://target/lambda-max.jar \
    --region us-east-1 \
    --role arn:aws:iam::000000000000:role/lambdamax

  echo "Created Lambda function: $FUNCTION_NAME"
done


