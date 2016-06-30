package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorResourceNotFound extends RuntimeException {

    public InteractorResourceNotFound(String resource) {
        super(resource + " not found");
    }
}
