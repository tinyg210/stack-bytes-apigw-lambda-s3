package apigw;

import java.io.IOException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuoteAppTests extends LocalStackConfig {

  public static final String BASE_URL = "/restapis/id12345/dev/_user_request_/quoteApi";

  @BeforeAll
  public static void setup() {
    setupConfig();
    localStack.followOutput(logConsumer);
    // wait 5 seconds to make sure the lambda is active
    try {
      Thread.sleep(5000);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @AfterAll
  public static void cleanUp() {
    cleanLambdaContainers();
  }

  @Test
  @Order(1)
  void testSuccessfulPostAction() {

    var postUrl =
        localStackEndpoint + "/restapis/id12345/dev/_user_request_/quoteApi";

    var expectedResponse =
        "{\"savedText\":\"{\\n  \\\"author\\\": \\\"Donkey\\\",\\n  \\\"text\\\":"
            + " \\\"I Like That Boulder. That Is A Nice Boulder.\\\"\\n}\","
            + "\"message\":\"New file has been added.\"}";

    // build the URL with the id as a path variable
    var getUrl = postUrl + "?author=";

    // set the request headers
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      // Add headers to a POST request
      var httpPost = new HttpPost(postUrl);
      httpPost.setHeader(new BasicHeader("Content-Type", "application/json"));
      // Create the JSON request body
      var jsonRequestBody = "{\n"
          + "\"author\":\"Donkey\",\n"
          + "\"text\":\"I Like That Boulder. That Is A Nice Boulder.\"\n"
          + "}";

      // Set the request body
      var entity = new StringEntity(jsonRequestBody);
      httpPost.setEntity(entity);
      // Execute the request
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        String responseBody = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assertions.assertEquals(expectedResponse, responseBody);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(2)
  void testSuccessfulGetAction() {

    var getUrl =
        localStackEndpoint + BASE_URL + "?author=";

    var expectedResponse = "{\"text\":\"Quote: I Like That Boulder. That Is A Nice Boulder.\"}";

    // set the request headers
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      // Add headers to a POST request
      var httpGet = new HttpGet(getUrl + "Donkey");
      httpGet.setHeader(new BasicHeader("Content-Type", "application/json"));

      // Execute the request
      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        String responseBody = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assertions.assertEquals(expectedResponse, responseBody);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
