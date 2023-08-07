terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "= 4.66.1"
    }
  }
}

resource "aws_s3_bucket" "quotes_bucket" {
  bucket = "quotes"
  force_destroy = true
  lifecycle {
    prevent_destroy = false
  }
}


resource "aws_lambda_function" "get_quote_lambda" {
  function_name = "get-quote"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "lambda.GetQuote::handleRequest"
  runtime       = "java17"
  filename      = "../../stack-bytes-lambda/target/apigw-lambda.jar"
  memory_size   = 512
  timeout       = 60
}

resource "aws_lambda_function" "create_quote_lambda" {
  function_name = "create-quote"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "lambda.CreateQuote::handleRequest"
  runtime       = "java17"
  filename      = "../../stack-bytes-lambda/target/apigw-lambda.jar"
  memory_size   = 512
  timeout       = 60
}

resource "aws_api_gateway_rest_api" "quote_api" {
  name        = "quote-api-gateway"
  description = "API Gateway for Quotes"
  tags = {
    _custom_id_ = "id12345"
  }
}

resource "aws_api_gateway_resource" "quote_resource" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  parent_id   = aws_api_gateway_rest_api.quote_api.root_resource_id
  path_part   = "quoteApi"
}

resource "aws_api_gateway_method" "get_method" {
  rest_api_id   = aws_api_gateway_rest_api.quote_api.id
  resource_id  = aws_api_gateway_resource.quote_resource.id
  http_method  = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_method" "post_method" {
  rest_api_id   = aws_api_gateway_rest_api.quote_api.id
  resource_id  = aws_api_gateway_resource.quote_resource.id
  http_method  = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "get_integration" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.get_method.http_method

  integration_http_method = "GET"
  type                    = "AWS_PROXY"
  uri                     = "arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:000000000000:function:get-quote/invocations"
}

resource "aws_api_gateway_integration" "post_integration" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.post_method.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = "arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:000000000000:function:create-quote/invocations"
}

resource "aws_api_gateway_method_response" "get_method_response" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.get_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_method_response" "post_method_response" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.post_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_integration_response" "get_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.get_method.http_method
  status_code = aws_api_gateway_method_response.get_method_response.status_code

  response_templates = {
    "application/json" = ""
  }
}

resource "aws_api_gateway_integration_response" "post_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.quote_api.id
  resource_id = aws_api_gateway_resource.quote_resource.id
  http_method = aws_api_gateway_method.post_method.http_method
  status_code = aws_api_gateway_method_response.post_method_response.status_code

  response_templates = {
    "application/json" = ""
  }
}

resource "aws_api_gateway_deployment" "quote_deployment" {
  depends_on      = [aws_api_gateway_integration.get_integration, aws_api_gateway_integration.post_integration, aws_api_gateway_method.get_method, aws_api_gateway_method.post_method]
  rest_api_id     = aws_api_gateway_rest_api.quote_api.id
  stage_name      = "dev"
}

resource "aws_iam_role" "lambda_execution" {
  name = "lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

