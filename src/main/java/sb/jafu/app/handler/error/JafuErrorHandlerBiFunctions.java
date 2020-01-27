package sb.jafu.app.handler.error;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.function.BiFunction;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public enum JafuErrorHandlerBiFunctions {

    HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION((t, request) -> {
        HttpMessageNotReadableException ee = (HttpMessageNotReadableException) t;
        String instanceDetails = "http message not readable: " + ee.getMostSpecificCause().getMessage();
        String instanceDebugDetails = "-";
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ee, instanceDetails, instanceDebugDetails);
        return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(APPLICATION_JSON).body(resp);
    }),

    JWTMALFORMEDEXCEPTION_HANDLER_FUCNTION((t, request) -> {
        MalformedJwtException ee = (MalformedJwtException) t;
        String instanceDetails = "http message not readable: " + ee.getMessage();
        String instanceDebugDetails = "-";
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ee, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.UNAUTHORIZED);
        return ServerResponse.status(HttpStatus.UNAUTHORIZED).contentType(APPLICATION_JSON).body(resp);
    }),

    JWTEXCEPTION_HANDLER_FUCNTION((t, request) -> {
        JwtException ee = (JwtException) t;
        String instanceDetails = "http message not readable: " + ee.getMessage();
        String instanceDebugDetails = "-";
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ee, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.UNAUTHORIZED);
        return ServerResponse.status(HttpStatus.UNAUTHORIZED).contentType(APPLICATION_JSON).body(resp);
    })


    ;

    JafuErrorHandlerBiFunctions(BiFunction<Throwable, ServerRequest, ServerResponse> handlerFunction) {
        this.handlerFunction = handlerFunction;
    }

    private final BiFunction<Throwable, ServerRequest, ServerResponse> handlerFunction;

    public BiFunction<Throwable, ServerRequest, ServerResponse> getHandlerFunction() {
        return handlerFunction;
    }
}
