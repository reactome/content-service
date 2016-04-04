package org.reactome.server.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class ContentServiceException extends RuntimeException {

    private HttpStatus httpStatus;
    private List<String> errorMessages = new LinkedList<>();

    ContentServiceException(HttpStatus httpStatus) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
    }

    public ContentServiceException(HttpStatus httpStatus, String message) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
        this.errorMessages.add(message);
    }

    public ContentServiceException(HttpStatus httpStatus, List<String> errorMessages) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
        if(errorMessages!=null){
            this.errorMessages = errorMessages;
        }
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
