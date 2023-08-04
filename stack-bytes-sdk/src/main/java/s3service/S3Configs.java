package s3service;

import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Configs {

  private static final String ACCESS_KEY = "test";
  private static final String SECRET_KEY = "test";
  private static final String LOCALSTACK_ENDPOINT = "https://s3.localhost.localstack.cloud:4566";
  private static Region region = Region.US_EAST_1;
  protected static final String BUCKET_NAME = "quotes";
  // create an S3 client
  protected static S3Client s3Client = S3Client.builder()
      .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
      .credentialsProvider(StaticCredentialsProvider.create(
          AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
      .region(region)
      .build();
}
