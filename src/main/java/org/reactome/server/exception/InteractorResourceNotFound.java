package org.reactome.server.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorResourceNotFound extends ContentServiceException {

    public InteractorResourceNotFound(String resource) {
        super(HttpStatus.NOT_FOUND, resource + " not found");
    }
}
