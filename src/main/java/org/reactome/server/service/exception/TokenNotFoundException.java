package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class TokenNotFoundException  extends RuntimeException {

    public TokenNotFoundException(String token) {
        super("Token [" + token + "] not found");
    }

}
