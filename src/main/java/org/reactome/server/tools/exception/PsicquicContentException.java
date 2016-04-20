package org.reactome.server.tools.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PsicquicContentException extends ContentServiceException {

    public PsicquicContentException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
