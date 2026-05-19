package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class ResponseError implements Serializable {

    @JsonProperty("response-code")
    private String responseCode;

    @JsonProperty("developer-message")
    private String developerMessage;

    @JsonProperty("user-message")
    private String userMessage;

    @JsonProperty("error-code")
    private String errorCode;

    @JsonProperty("more-info")
    private String moreInfo;

    public ResponseError() {
    }

    public ResponseError(String userMessage) {
        this.userMessage = userMessage;
    }

    public ResponseError(String errorCode, String userMessage) {
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public ResponseError(String responseCode, String developerMessage, String userMessage, String errorCode, String moreInfo) {
        this.responseCode = responseCode;
        this.developerMessage = developerMessage;
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.moreInfo = moreInfo;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    @Override
    public String toString() {
        return "ResponseError{" +
                "responseCode='" + responseCode + '\'' +
                ", developerMessage='" + developerMessage + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", moreInfo='" + moreInfo + '\'' +
                '}';
    }
}
