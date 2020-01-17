package sb.jafu.app.routes;

import org.springframework.web.servlet.function.RouterFunction;
import sb.jafu.app.handler.JafuApplicationRestHandler;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static sb.jafu.app.handler.error.JafuErrorHandlerBiFunctions.HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION;
import static sb.jafu.app.handler.error.JafuErrorHandlerPredicates.HTTPMESSAGENOTREADABLEEXCEPTION;

/**
 * @author SAROY on 1/17/2020
 */
@SuppressWarnings("rawtypes")
public class GeneralRoutes {

    private enum GeneralRouteDsl {
        ROUTE(handler -> () -> route().onError(HTTPMESSAGENOTREADABLEEXCEPTION.getPredicate(), HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION.getHandlerFunction())
                .GET("/text", accept(TEXT_PLAIN), handler::getTextResponse)
                .GET("/json", accept(APPLICATION_JSON), handler::getMessageJsonResponse)
                .POST("/json", accept(APPLICATION_JSON).and(contentType(APPLICATION_JSON)), handler::postMessageJson)
                .build());

        GeneralRouteDsl(Function<JafuApplicationRestHandler, Supplier<RouterFunction>> routeDslFunction) {
            this.routeDslFunction = routeDslFunction;
        }

        private final Function<JafuApplicationRestHandler, Supplier<RouterFunction>> routeDslFunction;

        Function<JafuApplicationRestHandler, Supplier<RouterFunction>> getRouteDslFunction() {
            return routeDslFunction;
        }
    }

    public static Function<JafuApplicationRestHandler, Supplier<RouterFunction>> getRoutes() {
        return GeneralRouteDsl.ROUTE.getRouteDslFunction();
    }
}
