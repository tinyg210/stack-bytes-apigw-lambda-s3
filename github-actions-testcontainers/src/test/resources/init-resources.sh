#!/bin/sh

apt-get -y install jq

# create bucket
echo "Create Bucket..."
awslocal s3api create-bucket --bucket quotes

# create lambda
echo "Create Quote Lambda..."
awslocal lambda create-function \
  --function-name create-quote \
  --runtime java17 \
  --handler lambda.CreateQuote::handleRequest \
  --memory-size 512 \
  --zip-file fileb:///etc/localstack/init/ready.d/target/apigw-lambda.jar \
  --region us-east-1 \
  --role arn:aws:iam::000000000000:role/apigw

echo "Get Quote Lambda..."
awslocal lambda create-function \
  --function-name get-quote \
  --runtime java17 \
  --handler lambda.GetQuote::handleRequest \
  --memory-size 512 \
  --zip-file fileb:///etc/localstack/init/ready.d/target/apigw-lambda.jar \
  --region us-east-1 \
  --role arn:aws:iam::000000000000:role/apigw

export REST_API_ID=id12345

# create rest api gateway
echo "Create Rest API..."
awslocal apigateway create-rest-api --name quote-api-gateway --tags '{"_custom_id_":"id12345"}'

# get parent id of resource
echo "Export Parent ID..."
export PARENT_ID=$(awslocal apigateway get-resources --rest-api-id id12345 | jq -r '.items[0].id')

# get resource id
echo "Export Resource ID..."
export RESOURCE_ID=$(awslocal apigateway create-resource --rest-api-id $REST_API_ID --parent-id $PARENT_ID --path-part "quoteApi" | jq -r '.id')

echo "Put GET Method..."
awslocal apigateway put-method \
--rest-api-id $REST_API_ID \
--resource-id $RESOURCE_ID \
--http-method GET \
--request-parameters "method.request.path.quoteApi=true" \
--authorization-type "NONE"


echo "Put POST Method..."
awslocal apigateway put-method \
--rest-api-id $REST_API_ID \
--resource-id $RESOURCE_ID \
--http-method POST \
--request-parameters "method.request.path.quoteApi=true" \
--authorization-type "NONE"


echo "Update GET Method..."
awslocal apigateway update-method \
  --rest-api-id $REST_API_ID \
  --resource-id $RESOURCE_ID \
  --http-method GET \
  --patch-operations "op=replace,path=/requestParameters/method.request.querystring.param,value=true"


echo "Put POST Method Integration..."
awslocal apigateway put-integration \
  --rest-api-id $REST_API_ID \
  --resource-id $RESOURCE_ID \
  --http-method POST \
  --type AWS_PROXY \
  --integration-http-method POST \
  --uri arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:000000000000:function:create-quote/invocations \
  --passthrough-behavior WHEN_NO_MATCH

echo "Put GET Method Integration..."
awslocal apigateway put-integration \
  --rest-api-id $REST_API_ID \
  --resource-id $RESOURCE_ID \
  --http-method GET \
  --type AWS_PROXY \
  --integration-http-method POST \
  --uri arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:000000000000:function:get-quote/invocations \
  --passthrough-behavior WHEN_NO_MATCH


echo "Create DEV Deployment..."
awslocal apigateway create-deployment \
  --rest-api-id $REST_API_ID \
  --stage-name dev

echo "Finished creating resources."