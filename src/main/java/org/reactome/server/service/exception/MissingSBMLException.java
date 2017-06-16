package org.reactome.server.service.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class MissingSBMLException extends RuntimeException {
    public MissingSBMLException(String message) {
        super(message);
    }
}
