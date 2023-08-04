package s3service;

import static s3service.S3Configs.BUCKET_NAME;
import static s3service.S3Configs.s3Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3GetRequest {


  public static void main(String[] args) {

    String objectKey = "Fiona.txt";

    try {
      // get the object from the S3 bucket
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(BUCKET_NAME)
          .key(objectKey)
          .build();

      ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

      // read the text content from the file
      String objectText = readResponseInputStream(response);

      System.out.println("Object text: " + objectText);

    } catch (S3Exception | IOException e) {
      System.err.println("Error reading object from S3: " + e.getMessage());
    }

    s3Client.close();
  }

  private static String readResponseInputStream(
      ResponseInputStream<GetObjectResponse> responseInputStream)
      throws IOException {
    try (InputStream inputStream = responseInputStream;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
      return stringBuilder.toString();
    }
  }

}
