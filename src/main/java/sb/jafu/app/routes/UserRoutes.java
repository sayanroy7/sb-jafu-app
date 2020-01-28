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
                                        .filter(SecurityHandlerFunction.hasAnyAuth("users:read")))
                                .nest(all(), b4 -> b4
                                        .POST("/json", contentType(APPLICATION_JSON), handler::postUserJsonResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("users:admin")))
                                .nest(all(), b5 -> b5.
                                        GET("/json/sys", accept(APPLICATION_JSON), handler::getNotAccessibleUser)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("system:admin")))
                                .nest(all(), b6 -> b6.
                                        GET("/json/customers", accept(APPLICATION_JSON), handler::getUserCustomerResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("customers:admin")))
                                .nest(all(), b7 -> b7.
                                        POST("/json/mongo", accept(APPLICATION_JSON), handler::saveUserMongoResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("mongo:admin")))
                                .nest(all(), b8 -> b8.
                                        GET("/json/mongo", accept(APPLICATION_JSON), handler::getAllUsersMongoResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("mongo:admin")))
                                .nest(all(), b9 -> b9.
                                        GET("/json/mongo/{id}", accept(APPLICATION_JSON), handler::getUserByIdMongoResponse)
                                        .filter(SecurityHandlerFunction.hasAnyAuth("mongo:admin")))
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
