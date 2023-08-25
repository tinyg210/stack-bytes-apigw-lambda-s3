package apigw;

import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Testcontainers
public class LocalStackConfig {

  @Container
  protected static LocalStackContainer localStack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack-pro:2.2.0"))
          .withEnv("LAMBDA_REMOVE_CONTAINERS", "1")
          .withEnv("LOCALSTACK_API_KEY", System.getenv("LOCALSTACK_API_KEY"))
          .withFileSystemBind("../stack-bytes-lambda/target/apigw-lambda.jar",
              "/etc/localstack/init/ready.d/target/apigw-lambda.jar")
          .withFileSystemBind("src/test/resources/init-resources.sh",
              "/etc/localstack/init/ready.d/init-resources.sh")
          .withEnv("DEBUG", "1")
          .withEnv("LAMBDA_KEEPALIVE_MS", "10000")
          .waitingFor(Wait.forLogMessage(".*Finished creating resources.*\\n", 1));

  protected static final Logger LOGGER = LoggerFactory.getLogger(LocalStackConfig.class);
  protected static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
  protected static URI localStackEndpoint;
  protected static LambdaClient lambdaClient;

  @BeforeAll()
  protected static void setupConfig() {
    localStackEndpoint = localStack.getEndpoint();

    lambdaClient = LambdaClient.builder()
        .region(Region.of(localStack.getRegion()))
        .endpointOverride(localStackEndpoint)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
        .build();
  }

  protected static void cleanLambdaContainers() {
    try {
      String scriptPath = "src/test/resources/delete_lambda_containers.sh";
      // ProcessBuilder to execute the script
      ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);
      // redirect the process's output to the java process's output
      processBuilder.inheritIO();
      Process process = processBuilder.start();
      // wait for the process to complete
      int exitCode = process.waitFor();
      // print the exit code for debugging purposes
      System.out.println("Script exited with code: " + exitCode);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}

