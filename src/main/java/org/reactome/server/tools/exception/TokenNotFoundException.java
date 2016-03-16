package org.reactome.server.tools.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class TokenNotFoundException  extends ContentServiceException {

    public TokenNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Token not found");
    }

}
