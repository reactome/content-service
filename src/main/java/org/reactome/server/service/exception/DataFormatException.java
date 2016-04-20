package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class DataFormatException extends ContentServiceException {

    public DataFormatException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public DataFormatException(List<String> errorMessages) {
        super(HttpStatus.BAD_REQUEST, errorMessages);
    }

}
