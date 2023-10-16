package apigw;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ResourceReaper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Testcontainers
public class LocalStackConfig {
  static Network network = Network.newNetwork();

  @Container
  protected static LocalStackContainer localStack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack-pro:latest"))
          .withEnv("LOCALSTACK_API_KEY", System.getenv("LOCALSTACK_API_KEY"))
          .withFileSystemBind("../stack-bytes-lambda/target/apigw-lambda.jar",
              "/etc/localstack/init/ready.d/target/apigw-lambda.jar")
          .withFileSystemBind("src/test/resources/init-resources.sh",
              "/etc/localstack/init/ready.d/init-resources.sh")
          .withEnv("DEBUG", "1")
          .withNetwork(network)
          .withEnv("LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl) network).getName())
          .withNetworkAliases("localstack")
          .withEnv("LAMBDA_DOCKER_FLAGS", testcontainersLabels())
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

  static String testcontainersLabels() {
    return Stream
        .of(DockerClientFactory.DEFAULT_LABELS.entrySet().stream(),
            ResourceReaper.instance().getLabels().entrySet().stream())
        .flatMap(Function.identity())
        .map(entry -> String.format("-l %s=%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(" "));
  }

}

