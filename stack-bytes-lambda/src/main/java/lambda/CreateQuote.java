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
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class CreateQuote extends QuoteApi implements RequestStreamHandler {

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    JSONObject responseJson = new JSONObject();
    try {
      JSONObject event = (JSONObject) parser.parse(reader);

      if (event.get("body") != null) {
        Quote quote = new Quote((String) event.get("body"));

        addQuoteFileToS3(quote);
        create200Response(responseJson, quote);

      }

    } catch (ParseException | BusinessException exception) {
      create400Response(responseJson, exception);
    }

    OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    writer.write(responseJson.toString());
    writer.close();
  }

  private void create200Response(JSONObject responseJson, Quote quote) {
    JSONObject responseBody = new JSONObject();
    responseBody.put("message", "New file has been added.");
    responseBody.put("savedText", quote.toString());

    JSONObject headerJson = new JSONObject();

    responseJson.put("statusCode", 200);
    responseJson.put("headers", headerJson);
    responseJson.put("body", responseBody.toString());
  }

  private void create400Response(JSONObject responseJson, Exception exception) {
    JSONObject responseBody = new JSONObject();
    responseBody.put("message", "Exception occurred.");
    responseBody.put("exception", exception.getMessage());

    JSONObject headerJson = new JSONObject();

    responseJson.put("statusCode", 400);
    responseJson.put("headers", headerJson);
    responseJson.put("body", responseBody.toString());

  }


  private void addQuoteFileToS3(Quote input) throws BusinessException {
    String author = input.getAuthor();
    String text = input.getText();

    if(author == null) {
      throw new BusinessException("The following field cannot be empty: author.");
    }
    if(text == null) {
      throw new BusinessException("The following field cannot be empty: text.");
    }

    // create content and file name
    String fileContent = "Author: " + author + "\n" + "Quote: " + text;
    String fileName = author + ".txt";

    SdkBytes fileBytes = SdkBytes.fromUtf8String(fileContent);
    s3Client.putObject(PutObjectRequest.builder()
        .bucket(BUCKET_NAME)
        .key(fileName)
        .contentLength((long) fileContent.length())
        .contentType("text/plain")
        .build(), RequestBody.fromBytes(fileBytes.asByteArray()));
  }

}

