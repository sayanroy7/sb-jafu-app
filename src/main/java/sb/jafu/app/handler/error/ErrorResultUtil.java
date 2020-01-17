/**
 * Copyright 2019 Sensus
 * All rights reserved
 */
package sb.jafu.app.handler.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ErrorResultUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorResultUtil.class);

    private ErrorResultUtil() {
    }

    public static CommonErrorResponse logAndGetCommonErrorResponse(Exception ex, String instanceDetails,
            String instanceDebugDetails) {
        if (LOG.isErrorEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("exception: ").append(ex.getClass().getCanonicalName());
            sb.append("stack trace: ").append(getStackTrace(ex));
            LOG.error(sb.toString());
        }
        return getCommonErrorResponse(instanceDetails, instanceDebugDetails);
    }

    public static CommonErrorResponse logAndGetCommonErrorResponseWithStatus(Exception ex, String instanceDetails,
                                                                   String instanceDebugDetails, HttpStatus status) {
        if (LOG.isErrorEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("exception: ").append(ex.getClass().getCanonicalName());
            sb.append("stack trace: ").append(getStackTrace(ex));
            LOG.error(sb.toString());
        }
        return getCommonErrorResponse(instanceDetails, instanceDebugDetails, status);
    }

    public static CommonErrorResponse getCommonErrorResponse(String instanceDetails, String instanceDebugDetails) {
        return CommonErrorResponseBuilder.builder()
                .instanceDetails(instanceDetails)
                .instanceDebugDetails(instanceDebugDetails)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
    }

    public static CommonErrorResponse getCommonErrorResponse(String instanceDetails, String instanceDebugDetails, HttpStatus status) {
        return CommonErrorResponseBuilder.builder()
                .instanceDetails(instanceDetails)
                .instanceDebugDetails(instanceDebugDetails)
                .status(status)
                .timestamp(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
    }

    private static String getStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static ResponseEntity<Object> handleOtherErrors(Exception ex,
                                                    @NonNull WebRequest webRequest) {
        String instanceDetails = "An unexpected error occurred";
        String instanceDebugDetails = "-";
        CommonErrorResponse resp = logAndGetCommonErrorResponse(ex, instanceDetails, instanceDebugDetails);
        return new ResponseEntity<>(resp, resp.getStatus());
    }

}
