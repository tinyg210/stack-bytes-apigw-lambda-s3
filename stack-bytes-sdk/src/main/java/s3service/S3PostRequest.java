package s3service;

import static s3service.S3Configs.BUCKET_NAME;
import static s3service.S3Configs.s3Client;

import java.nio.file.Paths;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3PostRequest {


  public static void main(String[] args) {

    // local file to upload
    String filePath = "src/main/resources/Fiona.txt";
    String objectKey = "Fiona.txt";

    try {
      // put the object into the S3 bucket
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(BUCKET_NAME)
          .key(objectKey)
          .build();

      PutObjectResponse response = s3Client.putObject(putObjectRequest, Paths.get(filePath));

      if (response != null) {
        System.out.println("Object uploaded successfully!");
      } else {
        System.out.println("Object upload failed!");
      }
    } catch (S3Exception s3Exception) {
      System.out.println("An error occurred: " + s3Exception.getMessage());
    }

    s3Client.close();
  }
}
