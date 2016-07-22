package org.reactome.server.service.controller.graph;

import org.reactome.server.service.exception.NotFoundException;

/**
 * Some of our services produce text/plain instead of application/json
 * and in case of an error the @ExceptionHandler will try to serialize to json
 * but it colides to what the method is actually producing - which is text plain.
 * For these cases we have another exception handling.
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class NotFoundTextPlainException extends NotFoundException {

    public NotFoundTextPlainException(String message) {
        super(message);
    }

}
