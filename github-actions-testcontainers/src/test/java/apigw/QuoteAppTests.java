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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;
import software.amazon.awssdk.services.lambda.waiters.LambdaWaiter;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuoteAppTests extends LocalStackConfig {

  public static final String BASE_URL = "/restapis/id12345/dev/_user_request_/quoteApi";

  @BeforeAll
  public static void setup() {
    setupConfig();

    localStack.followOutput(logConsumer);

    LambdaWaiter waiter = lambdaClient.waiter();
    GetFunctionRequest getFunctionRequest = GetFunctionRequest.builder()
        .functionName("create-quote")
        .build();
    WaiterResponse<GetFunctionResponse> waiterResponse = waiter.waitUntilFunctionActiveV2(
        getFunctionRequest);
    waiterResponse.matched().response().ifPresent(response -> LOGGER.info(response.toString()));

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

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      // add headers to a POST request
      var httpPost = new HttpPost(postUrl);
      httpPost.setHeader(new BasicHeader("Content-Type", "application/json"));
      // create the JSON request body
      var jsonRequestBody = "{\n"
          + "\"author\":\"Donkey\",\n"
          + "\"text\":\"I Like That Boulder. That Is A Nice Boulder.\"\n"
          + "}";

      // set the request body
      var entity = new StringEntity(jsonRequestBody);
      httpPost.setEntity(entity);
      // execute the request
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

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      // add headers to the GET request
      var httpGet = new HttpGet(getUrl + "Donkey");
      httpGet.setHeader(new BasicHeader("Content-Type", "application/json"));

      // execute the request
      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        String responseBody = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assertions.assertEquals(expectedResponse, responseBody);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(3)
  void testExceptionIsThrownOnPostWrongJSON() {
    var postUrl =
        localStackEndpoint + "/restapis/id12345/dev/_user_request_/quoteApi";

    var expectedResponse =
        "{\"exception\":\"The following field cannot be empty: author.\",\"message\":\"Exception occurred.\"}";

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      // add headers to a POST request
      var httpPost = new HttpPost(postUrl);
      httpPost.setHeader(new BasicHeader("Content-Type", "application/json"));
      // create the JSON request body
      var jsonRequestBody = "{\n"
          + "\"text\":\"I Like That Boulder. That Is A Nice Boulder.\"\n"
          + "}";

      // set the request body
      var entity = new StringEntity(jsonRequestBody);
      httpPost.setEntity(entity);
      // execute the request
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        String responseBody = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        Assertions.assertEquals(expectedResponse, responseBody);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
