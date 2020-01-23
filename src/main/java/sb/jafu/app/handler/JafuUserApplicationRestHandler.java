package sb.jafu.app.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.yaml.snakeyaml.Yaml;
import sb.jafu.app.client.JafuRestClient;
import sb.jafu.app.client.RestClientException;
import sb.jafu.app.model.SlackMessage;
import sb.jafu.app.model.User;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.servlet.function.ServerResponse.ok;
import static org.springframework.web.servlet.function.ServerResponse.status;

/**
 * @author SAROY on 1/17/2020
 */
public class JafuUserApplicationRestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JafuUserApplicationRestHandler.class);

    private static final String SLACK_BOT_OAUTH;

    static {
        String slackBotToken = null;
        //from "spring.cloud.bootstrap.location" specified application.yml file.
        String bootstrapConfigLocation = System.getProperty("spring.config.location");
        Path yamlFilePath = null;
        if (bootstrapConfigLocation == null) {
            bootstrapConfigLocation = "application.yml";
            try {
                yamlFilePath = Paths.get(new ClassPathResource(bootstrapConfigLocation).getURI());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            yamlFilePath = Paths.get(bootstrapConfigLocation);
        }

        try (InputStream inputStream = Files.newInputStream(yamlFilePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlConfigs = yaml.load(inputStream);
            @SuppressWarnings("unchecked") Map<String, Object> slackYamlConfigs = (Map<String, Object>) yamlConfigs.get("slack");
            @SuppressWarnings("unchecked") Map<String, Object> slackBotYamlConfigs = (Map<String, Object>) slackYamlConfigs.get("bot");
            slackBotToken = (String) slackBotYamlConfigs.get("token");
            if (StringUtils.isEmpty(slackBotToken)) {
                throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load slack bot token details from key \"slack.bot.token\"" +
                        " located in \"application.yml\"");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load \"application.yml\" || " +
                    e.getMessage(), e);
        }

        //If there is no error from above executions, it is considered SLACK_BOT_OAUTH is loaded successfully.
        SLACK_BOT_OAUTH = "Bearer " + slackBotToken;
    }


    private JafuRestClient jafuRestClient;

    public JafuUserApplicationRestHandler(JafuRestClient jafuRestClient) {
        this.jafuRestClient = jafuRestClient;
    }

    public ServerResponse getUserJsonResponse(ServerRequest request) throws JsonProcessingException {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setMobile(8878787778L);
        user.setUserMetadata(Collections.singletonMap("location", "planet Earth"));

        return ok().contentType(APPLICATION_JSON).body(user);
    }


    public ServerResponse postUserJsonResponse(ServerRequest request) throws IOException, ServletException {
        User user = request.body(User.class);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, SLACK_BOT_OAUTH);

        SlackMessage slackMessage = new SlackMessage();
        slackMessage.setChannel("CRC2W0Q3F");
        slackMessage.setText("New User POSTED to JAFU with username: "+ user.getUsername());
        HttpEntity<SlackMessage> httpEntity = new HttpEntity<>(slackMessage, httpHeaders);
        ResponseEntity<String> responseEntity = jafuRestClient.execute("https://slack.com/api/chat.postMessage", HttpMethod.POST, httpEntity, String.class);

        return status(responseEntity.getStatusCode()).contentType(APPLICATION_JSON).body(user);
    }

}
