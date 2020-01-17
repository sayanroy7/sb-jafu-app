package sb.jafu.app.handler.error;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/**
 * @author SAROY on 1/16/2020
 */
public final class CommonErrorResponseBuilder {
    private String typeMessage;
    private String instanceDetails;
    private String instanceDebugDetails = null;
    private ZonedDateTime timestamp;
    private HttpStatus status;

    private CommonErrorResponseBuilder() {
    }

    public static CommonErrorResponseBuilder builder() {
        return new CommonErrorResponseBuilder();
    }

    public CommonErrorResponseBuilder typeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
        return this;
    }

    public CommonErrorResponseBuilder instanceDetails(String instanceDetails) {
        this.instanceDetails = instanceDetails;
        return this;
    }

    public CommonErrorResponseBuilder instanceDebugDetails(String instanceDebugDetails) {
        this.instanceDebugDetails = instanceDebugDetails;
        return this;
    }

    public CommonErrorResponseBuilder timestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public CommonErrorResponseBuilder status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public CommonErrorResponse build() {
        CommonErrorResponse commonErrorResponse = new CommonErrorResponse();
        commonErrorResponse.setTypeMessage(typeMessage);
        commonErrorResponse.setInstanceDetails(instanceDetails);
        commonErrorResponse.setInstanceDebugDetails(instanceDebugDetails);
        commonErrorResponse.setTimestamp(timestamp);
        commonErrorResponse.setStatus(status);
        return commonErrorResponse;
    }
}
