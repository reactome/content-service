package org.reactome.server.service.exception;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class MissingSBXXException extends Exception {
    public MissingSBXXException(String message) {
        super(message);
    }
}
