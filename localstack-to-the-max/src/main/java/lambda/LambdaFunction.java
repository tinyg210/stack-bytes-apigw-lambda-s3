package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LambdaFunction implements RequestStreamHandler {

  private JSONParser parser = new JSONParser();

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream,
      Context context) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    try {
      JSONObject event = (JSONObject) parser.parse(reader);

      if (event.get("body") != null) {
        System.out.println("Hello from the function number: " + event.get("body"));
      }
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

}


