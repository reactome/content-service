package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class RequestEntityTooLargeException extends RuntimeException {

    public RequestEntityTooLargeException() {
        super("Requested entity is too large");
    }

}
