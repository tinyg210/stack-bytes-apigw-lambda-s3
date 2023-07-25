package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class GetQuote extends QuoteApi implements RequestStreamHandler {

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {
    JSONParser parser = new JSONParser();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    JSONObject responseJson = new JSONObject();
    String text = null;
    try {
      JSONObject event = (JSONObject) parser.parse(reader);
      JSONObject responseBody = new JSONObject();

      if (event.get("queryStringParameters") != null) {
        JSONObject qparam = (JSONObject) event.get("queryStringParameters");
        if (qparam.get("author") != null) {

          String fileName = (String) qparam.get("author") + ".txt";
          text = readSecondLineFromS3File(s3Client, fileName);

        }
      }
      if (text != null) {
        responseBody.put("text", text);
        responseJson.put("statusCode", 200);
      } else {
        responseBody.put("message", "No item found");
        responseJson.put("statusCode", 404);
      }

      responseJson.put("body", responseBody.toString());

    } catch (ParseException pex) {
      responseJson.put("statusCode", 400);
      responseJson.put("exception", pex);
    }

    OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    writer.write(responseJson.toString());
    writer.close();
  }

  private static String readSecondLineFromS3File(S3Client s3Client,
      String fileName) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(BUCKET_NAME)
          .key(fileName)
          .build();

      ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
      BufferedReader reader = new BufferedReader(new InputStreamReader(response));
      // Read the first line
      reader.readLine();

      // Read and return the second line
      return reader.readLine();
    } catch (S3Exception | IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
