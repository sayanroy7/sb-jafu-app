/**
 * Copyright 2019 Sensus
 * All rights reserved
 */
package sb.jafu.app.handler.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Sanitize Rest Exceptions
 * Following is only applicable to our controllers. If request has not been mapped to our controller,
 * then these handlers are not referenced.
 * We need to hide internals from the clients.
 * Respective services should provide their own @RestControllerAdvice class and have it extend this class.
 * This will provide a uniform filter on various common exceptions.
 * For most flows, we need to add a unique request ID then:
 * - log details against that ID
 * - if prod:
 * provide a sanitized response to the client that includes that ID
 * else
 * provide the full response to the client that includes that ID and details
 */
public class JafuResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JafuResponseEntityExceptionHandler.class);

    private final Environment env;

    public JafuResponseEntityExceptionHandler(Environment env) {
        this.env = env;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        StringBuilder sb = new StringBuilder()
                .append("method arguments not valid: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            sb.append(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            sb.append(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        String instanceDetails = sb.toString();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "http message not readable: " + ex.getMostSpecificCause().getMessage();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        StringBuilder sb = new StringBuilder()
                .append("http request method not supported. request is not one of supported methods: ")
                .append(ex.getSupportedHttpMethods().stream().map(errorMessage -> errorMessage.name()).collect(Collectors.joining(",")));
        String instanceDetails = sb.toString();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        StringBuilder sb = new StringBuilder()
                .append("http media type not supported. media type is not one of supported types: ")
                .append(ex.getSupportedMediaTypes().stream().map(errorMessage -> errorMessage.getType()).collect(Collectors.joining(",")));
        String instanceDetails = sb.toString();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(@NonNull HttpMediaTypeNotAcceptableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        StringBuilder sb = new StringBuilder()
                .append("http media type not acceptable. media type is not one of supported types: ")
                .append(ex.getSupportedMediaTypes().stream().map(errorMessage -> errorMessage.getType()).collect(Collectors.joining(",")));
        String instanceDetails = sb.toString();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.NOT_ACCEPTABLE);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(@NonNull MissingPathVariableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "missing path variable: " + ex.getVariableName();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "missing servlet request parameter: " + ex.getParameterName();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(@NonNull ServletRequestBindingException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {

        String instanceDetails = "servlet request binding exception: " + ex.getMessage();

        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);

        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(@NonNull ConversionNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "conversion not supported: conversion failed for property " + ex.getPropertyName() + " of type "
                + ex.getRequiredType().getName();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(@NonNull TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "type mismatch: conversion failed for property " + ex.getPropertyName() + " of type " + ex.getRequiredType().getName();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(@NonNull HttpMessageNotWritableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "http message not writable: " + ex.getMessage();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(@NonNull MissingServletRequestPartException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = ex.getRequestPartName() + " is missing from servlet request";
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(@NonNull BindException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        StringBuilder sb = new StringBuilder()
                .append("bind exception: ");
        for (ObjectError error : ex.getAllErrors()) {
            sb.append(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        String instanceDetails = sb.toString();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "no handler found for " + ex.getHttpMethod() + ": " + ex.getMessage();
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.NOT_FOUND);
        return getSanitizedResponseObj(resp);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(@NonNull AsyncRequestTimeoutException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest webRequest) {
        String instanceDetails = "async request timed out";
        String instanceDebugDetails = getCommonInstanceDebugDetails(ex);
        CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        resp.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        return getSanitizedResponseObj(resp);
    }

    private String getCommonInstanceDebugDetails(Exception ex) {
        // for now, this is just a placeholder
        return "-";
    }

    private ResponseEntity<CommonErrorResponse> getSanitizedResponse(CommonErrorResponse resp) {
        logAndFilter(resp);
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    private ResponseEntity<Object> getSanitizedResponseObj(CommonErrorResponse resp) {
        logAndFilter(resp);
        return new ResponseEntity<>(resp, resp.getStatus());
    }

    private void logAndFilter(CommonErrorResponse resp) {
        LOG.error(resp.toString());
        if (Arrays.asList(this.env.getActiveProfiles()).contains("prod")) {
            resp.setInstanceDebugDetails(null);
        }
    }

}
