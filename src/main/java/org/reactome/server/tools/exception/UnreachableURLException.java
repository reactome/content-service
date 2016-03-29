package org.reactome.server.tools.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class UnreachableURLException extends ContentServiceException {

    public UnreachableURLException(int statusCode) {
        super(HttpStatus.valueOf(statusCode));
    }

}
