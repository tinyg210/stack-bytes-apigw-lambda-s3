package lambda;

import java.net.URI;
import org.json.simple.parser.JSONParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class QuoteApi {
  protected static final String S3_REGION = "us-east-1";
  protected static final String BUCKET_NAME = "quotes";
  protected JSONParser parser = new JSONParser();

  protected S3Client s3Client = S3Client.builder()
      .region(Region.of(S3_REGION))
      .endpointOverride(URI.create("http://localstack:4566"))
      .forcePathStyle(true)
      .build();
}
