package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PsicquicContentException extends RuntimeException {

    public PsicquicContentException(String message) {
        super(message);
    }
}
