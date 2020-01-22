package sb.jafu.app.client;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

/**
 * @author SAROY on 1/17/2020
 */
public class RestClientException extends NestedRuntimeException {

    private final HttpStatus httpStatus;

    public RestClientException(HttpStatus httpStatus, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
    }

    public RestClientException(HttpStatus httpStatus, String msg, Throwable t) {
        super(msg, t);
        this.httpStatus = httpStatus;
    }
}
