package sb.jafu.app.handler.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author SAROY on 1/16/2020
 */
public class JafuExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JafuExceptionHandlerExceptionResolver.class);

    public JafuExceptionHandlerExceptionResolver() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected ModelAndView doResolveHandlerMethodException(
            HttpServletRequest request, HttpServletResponse response,
            HandlerMethod handlerMethod, Exception exception) {

        LOGGER.error("Exception Handling... ", exception);

        ResponseEntityExceptionHandler handler = getApplicationContext().getBean(ResponseEntityExceptionHandler.class);
        ResponseEntity<?> responseEntity = null;
        ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);
        try {
            responseEntity = handler.handleException(exception, servletWebRequest);
        } catch (Exception invocationEx) {
            // Any other than the original exception (or its cause) is unintended here,
            // probably an accident (e.g. failed assertion or the like).
            if (invocationEx != exception && invocationEx != exception.getCause() && logger.isWarnEnabled()) {
                LOGGER.warn("Failure in @ExceptionHandler.. ", invocationEx);
            }
            // Continue with default processing of the original exception...
            return null;
        }

        if (responseEntity == null) {
            responseEntity = ErrorResultUtil.handleOtherErrors(exception, servletWebRequest);
        }

        response.setStatus(responseEntity.getStatusCode().value());
        try (ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response)) {
            for (HttpMessageConverter<?> messageConverter : getMessageConverters()) {
                if (messageConverter instanceof GenericHttpMessageConverter<?>) {
                    GenericHttpMessageConverter<Object> genericMessageConverter =
                            (GenericHttpMessageConverter<Object>) messageConverter;
                    if (genericMessageConverter.canWrite(CommonErrorResponse.class, CommonErrorResponse.class, MediaType.APPLICATION_JSON)) {
                        try {
                            genericMessageConverter.write(responseEntity.getBody(), CommonErrorResponse.class, MediaType.APPLICATION_JSON, outputMessage);
                            break;
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
                if (messageConverter.canWrite(CommonErrorResponse.class, MediaType.APPLICATION_JSON)) {
                    try {
                        ((HttpMessageConverter<Object>) messageConverter).write(responseEntity.getBody(), MediaType.APPLICATION_JSON, outputMessage);
                        break;
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
        return new ModelAndView();
    }

}
