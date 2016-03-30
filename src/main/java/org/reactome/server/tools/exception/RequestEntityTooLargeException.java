package org.reactome.server.tools.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class RequestEntityTooLargeException extends ContentServiceException {

    public RequestEntityTooLargeException() {
        super(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
    }

}
