package sb.jafu.app.routes;

import org.springframework.web.servlet.function.RouterFunction;
import sb.jafu.app.handler.JafuUserApplicationRestHandler;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static sb.jafu.app.handler.error.JafuErrorHandlerBiFunctions.HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION;
import static sb.jafu.app.handler.error.JafuErrorHandlerPredicates.HTTPMESSAGENOTREADABLEEXCEPTION;

/**
 * @author SAROY on 1/17/2020
 */
@SuppressWarnings("rawtypes")
public class UserRoutes {

    private enum UserRouteDsl {
        ROUTE(handler -> () -> route().onError(HTTPMESSAGENOTREADABLEEXCEPTION.getPredicate(), HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION.getHandlerFunction())
                .path("/user", b1 -> b1.
                        nest(accept(APPLICATION_JSON), b2 -> b2.
                                GET("/json", accept(APPLICATION_JSON), handler::getUserJsonResponse)
                        )
                )
                .build());

        UserRouteDsl(Function<JafuUserApplicationRestHandler, Supplier<RouterFunction>> routeDslFunction) {
            this.routeDslFunction = routeDslFunction;
        }

        private final Function<JafuUserApplicationRestHandler, Supplier<RouterFunction>> routeDslFunction;

        Function<JafuUserApplicationRestHandler, Supplier<RouterFunction>> getRouteDslFunction() {
            return routeDslFunction;
        }
    }

    public static Function<JafuUserApplicationRestHandler, Supplier<RouterFunction>> getRoutes() {
        return UserRoutes.UserRouteDsl.ROUTE.getRouteDslFunction();
    }


}
