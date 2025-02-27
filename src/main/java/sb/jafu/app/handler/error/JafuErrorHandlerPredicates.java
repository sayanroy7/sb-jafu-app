package sb.jafu.app.handler.error;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.function.Predicate;

/**
 * @author SAROY on 1/16/2020
 */
public enum  JafuErrorHandlerPredicates {

    HTTPMESSAGENOTREADABLEEXCEPTION(e -> e instanceof HttpMessageNotReadableException),
    JWTMALFORMEDEXCEPTION(e -> e instanceof MalformedJwtException),
    JWTEXCEPTION(e -> e instanceof JwtException)
    ;

    JafuErrorHandlerPredicates(Predicate<Throwable> predicate) {
        this.predicate = predicate;
    }

    private final Predicate<Throwable> predicate;

    public Predicate<Throwable> getPredicate() {
        return predicate;
    }
}
