package org.reactome.server.service.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class MissingSBMLException extends Exception {
    public MissingSBMLException(String message) {
        super(message);
    }
}
