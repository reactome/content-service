package org.reactome.server.orcid.exception;

import org.reactome.server.orcid.domain.ResponseError;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class WorkClaimException extends Exception {
    private ResponseError responseError;

    public WorkClaimException(){
    }

    public WorkClaimException(String message) {
        super(message);
    }

    public WorkClaimException(String msg, ResponseError responseError) {
        super(msg);
        this.responseError = responseError;
    }

    public WorkClaimException(ResponseError responseError) {
        this.responseError = responseError;
    }

    public ResponseError getResponseError() {
        return responseError;
    }

    public void setResponseError(ResponseError responseError) {
        this.responseError = responseError;
    }
}
