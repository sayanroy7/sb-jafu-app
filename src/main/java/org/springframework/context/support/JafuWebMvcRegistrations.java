package org.springframework.context.support;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import sb.jafu.app.handler.error.JafuExceptionHandlerExceptionResolver;

/**
 * @author SAROY on 1/16/2020
 */
public class JafuWebMvcRegistrations implements WebMvcRegistrations {

    @Override
    public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
        return new JafuExceptionHandlerExceptionResolver();
    }
}
