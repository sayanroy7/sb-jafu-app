package sb.jafu.app.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import sb.jafu.app.client.JafuRestClient;
import sb.jafu.app.model.SlackMessage;
import sb.jafu.app.model.User;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.servlet.function.ServerResponse.ok;
import static org.springframework.web.servlet.function.ServerResponse.status;

/**
 * @author SAROY on 1/17/2020
 */
public class JafuUserApplicationRestHandler {

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
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "");

        SlackMessage slackMessage = new SlackMessage();
        slackMessage.setChannel("CRC2W0Q3F");
        slackMessage.setText("New User POSTED to JAFU with username: "+ user.getUsername());
        HttpEntity<SlackMessage> httpEntity = new HttpEntity<>(slackMessage, httpHeaders);
        ResponseEntity<String> responseEntity = jafuRestClient.execute("https://slack.com/api/chat.postMessage", HttpMethod.POST, httpEntity, String.class);

        return status(responseEntity.getStatusCode()).contentType(APPLICATION_JSON).body(user);
    }

}
