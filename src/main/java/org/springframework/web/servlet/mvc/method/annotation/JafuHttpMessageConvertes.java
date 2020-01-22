package org.springframework.web.servlet.mvc.method.annotation;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public enum JafuHttpMessageConvertes {
    BYTE_ARRAY_CONVERTER(new ByteArrayHttpMessageConverter()),
    JACKSON_2_CONVERTER(new MappingJackson2HttpMessageConverter()),
    STRING_CONVERTER(new StringHttpMessageConverter())
    ;

    JafuHttpMessageConvertes(HttpMessageConverter<?> httpMessageConverter) {
        this.httpMessageConverter = httpMessageConverter;
    }

    private final HttpMessageConverter<?> httpMessageConverter;

    public HttpMessageConverter<?> getHttpMessageConverter() {
        return httpMessageConverter;
    }
}
