/**
 * Copyright 2019 Sensus
 * All rights reserved
 */
package sb.jafu.app.handler.error;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class CommonErrorResponse {

    private String typeMessage;

    private String instanceDetails;

    private String instanceDebugDetails = null;

    private ZonedDateTime timestamp;

    private HttpStatus status;

    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getInstanceDetails() {
        return instanceDetails;
    }

    public void setInstanceDetails(String instanceDetails) {
        this.instanceDetails = instanceDetails;
    }

    public String getInstanceDebugDetails() {
        return instanceDebugDetails;
    }

    public void setInstanceDebugDetails(String instanceDebugDetails) {
        this.instanceDebugDetails = instanceDebugDetails;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
