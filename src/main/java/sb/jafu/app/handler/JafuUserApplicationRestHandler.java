package sb.jafu.app.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import sb.jafu.app.model.User;

import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;

/**
 * @author SAROY on 1/17/2020
 */
public class JafuUserApplicationRestHandler {

    public ServerResponse getUserJsonResponse(ServerRequest request) throws JsonProcessingException {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setMobile(8878787778L);
        user.setUserMetadata(Collections.singletonMap("location", "planet Earth"));

        return ok().contentType(APPLICATION_JSON).body(user);
    }

}
