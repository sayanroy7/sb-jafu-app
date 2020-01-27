package sb.jafu.app.routes;

import org.springframework.web.servlet.function.RouterFunction;
import sb.jafu.app.handler.JafuUserApplicationRestHandler;
import sb.jafu.app.security.SecurityHandlerFunction;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static sb.jafu.app.handler.error.JafuErrorHandlerBiFunctions.*;
import static sb.jafu.app.handler.error.JafuErrorHandlerPredicates.*;

/**
 * @author SAROY on 1/17/2020
 */
@SuppressWarnings("rawtypes")
public class UserRoutes {

    private enum UserRouteDsl {
        ROUTE(handler -> () -> route()
                .onError(HTTPMESSAGENOTREADABLEEXCEPTION.getPredicate(), HTTPMESSAGENOTREADABLE_HANDLER_FUCNTION.getHandlerFunction())
                .onError(JWTMALFORMEDEXCEPTION.getPredicate(), JWTMALFORMEDEXCEPTION_HANDLER_FUCNTION.getHandlerFunction())
                .onError(JWTEXCEPTION.getPredicate(), JWTEXCEPTION_HANDLER_FUCNTION.getHandlerFunction())
                .path("/user", b1 -> b1.
                        nest(accept(APPLICATION_JSON), b2 -> b2.
                                nest(all(), b3 -> b3.GET("/json", handler::getUserJsonResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("data:raw:admin")))
                                .nest(all(), b4 -> b4
                                        .POST("/json", contentType(APPLICATION_JSON), handler::postUserJsonResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("data:ogc:admin")))
                                .nest(all(), b5 -> b5.
                                        GET("/nauser", accept(APPLICATION_JSON), handler::getNotAccessibleUser)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("mars:scope")))
                                .nest(all(), b6 -> b6.
                                        GET("/customers", accept(APPLICATION_JSON), handler::getUserCustomerResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("customers:admin")))
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
