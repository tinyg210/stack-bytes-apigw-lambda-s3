package apigw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class ApiGWPostRequestTest {

  private CloseableHttpClient httpClient = HttpClients.createDefault();
  private String apiGWEndpoint = "http://localhost:4566/restapis/id12345/dev/_user_request_/quoteApi"; // the API Gateway URL
  private String bucketName = "quotes";

  private S3Client s3Client = S3Client.builder().endpointOverride(
      URI.create("http://s3.localhost.localstack.cloud:4566")).build();

  @Test
  public void testPostRequestIsSuccessful() throws IOException {

    HttpPost httpPost = new HttpPost(apiGWEndpoint);

    // JSON payload & headers
    String jsonPayload = "{\n"
        + "    \"author\":\"Donkey\",\n"
        + "    \"text\":\"And in the morning...I'm making waffles!\"\n"
        + "}";
    StringEntity entity = new StringEntity(jsonPayload);
    httpPost.setEntity(entity);
    httpPost.setHeader("Content-Type", "application/json");

    // execute POST request
    HttpResponse response = httpClient.execute(httpPost);

    // validate the response
    assertEquals(200, response.getStatusLine().getStatusCode());

    HttpEntity responseEntity = response.getEntity();
    String responseBody = EntityUtils.toString(responseEntity);

    // check expected response
    String expectedResponse = "{\"savedText\":\"{\\n  \\\"author\\\": \\\"Donkey\\\",\\n  "
        + "\\\"text\\\": \\\"And in the morning...I\\\\u0027m making waffles!\\\"\\n}\",\"message\":\"New file has been added.\"}";
    assertEquals(expectedResponse, responseBody);

    String key = "Donkey.txt";

    // check if the file exists by sending a head request
    HeadObjectRequest headRequest = HeadObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

    HeadObjectResponse headResponse = s3Client.headObject(headRequest);
    assertEquals(200, headResponse.sdkHttpResponse().statusCode());

  }

  @Test
  public void testPostRequestIsFail() throws IOException {

    HttpPost httpPost = new HttpPost(apiGWEndpoint);

    // JSON payload & headers
    String jsonPayload = "{\n"
        + "    \"auth\":\"Shrek\",\n"
        + "    \"text\":\"Ogres are like onions. We have layers!\"\n"
        + "}";
    StringEntity entity = new StringEntity(jsonPayload);
    httpPost.setEntity(entity);
    httpPost.setHeader("Content-Type", "application/json");

    // execute POST request
    HttpResponse response = httpClient.execute(httpPost);

    // validate the response
    assertEquals(400, response.getStatusLine().getStatusCode());

    HttpEntity responseEntity = response.getEntity();
    String responseBody = EntityUtils.toString(responseEntity);

    // check expected response
    String expectedResponse = "{\"exception\":\"The following field cannot be empty: author.\",\"message\":\"Exception occurred.\"}";
    assertEquals(expectedResponse, responseBody);

    // check if the file exists by sending a head request
    HeadObjectRequest headRequest = HeadObjectRequest.builder()
        .bucket(bucketName)
        .key("Shrek.txt")
        .build();

    assertThrows(NoSuchKeyException.class, () -> s3Client.headObject(headRequest));

  }
}
