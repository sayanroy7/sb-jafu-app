package com.example.jafu.handler;

import com.example.jafu.model.Message;
import com.example.jafu.model.Metadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;
/**
 * @author SAROY on 1/15/2020
 */
public class JafuApplicationRestHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ServerResponse getTextResponse(ServerRequest request) {
        String textPlain = "This is text/plain message from Jafu RestHandler which is similar to @RestController";
        return ok().contentType(MediaType.TEXT_PLAIN).body(textPlain);
    }

    public ServerResponse getMessageJsonResponse(ServerRequest request) throws JsonProcessingException {
        Message message = new Message();
        message.setId("message1");
        message.setSerial(1234567);
        message.setCode(4L);
        message.setVersion(0.1258D);

        Metadata metadata = new Metadata();
        metadata.setId(2131354);
        metadata.setName("metadata1");
        metadata.setValues(Collections.singletonMap("type", "metadata"));
        message.setMetadata(metadata);

        Map<String, String> props = new HashMap<>();
        props.put("type", "message");
        props.put("status", "approved");
        message.setProperties(props);


        return ok().contentType(APPLICATION_JSON).body(message);
    }

}
