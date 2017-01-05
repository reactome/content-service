package org.reactome.server.service.exception;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class UnprocessableEntityException extends RuntimeException {

    public UnprocessableEntityException() {
        super();
    }
}
