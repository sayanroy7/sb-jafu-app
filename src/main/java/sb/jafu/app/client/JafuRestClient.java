package sb.jafu.app.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author SAROY on 1/17/2020
 */
public class JafuRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JafuRestClient.class);

    private RestTemplate restTemplate;

    public JafuRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> ResponseEntity<T> execute(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        try {
            LOGGER.info("*********************************************************************");
            LOGGER.info("START : {}-{} ", method, url);
            LOGGER.info("request {}", requestEntity);
            long startTime = System.currentTimeMillis();
            ResponseEntity<T> responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            long endTime = System.currentTimeMillis();
            LOGGER.info("END : {}ms-{}-{}-{} ", (endTime - startTime), responseEntity.getStatusCode(), method, url);
            LOGGER.debug("response {}", responseEntity);
            LOGGER.info("*********************************************************************");
            return responseEntity;
        } catch (HttpClientErrorException ex) {
            LOGGER.error(ex.getResponseBodyAsString(), ex);
            throw new RestClientException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong!",e);
        }

    }


}
