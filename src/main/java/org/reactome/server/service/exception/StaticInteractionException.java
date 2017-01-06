package org.reactome.server.service.exception;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class StaticInteractionException extends RuntimeException {

    public StaticInteractionException(Throwable t) {
        super(t);
    }
}
