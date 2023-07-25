
awslocal lambda update-function-code --function-name get-quote \
         --zip-file fileb://target/apigw-lambda.jar \
         --region us-east-1
