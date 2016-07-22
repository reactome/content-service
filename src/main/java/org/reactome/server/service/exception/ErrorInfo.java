package org.reactome.server.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
public class ErrorInfo {

    public final int code;
    public final HttpStatus reason;
    public String[] message = null;

    public ErrorInfo(HttpStatus status, String... message) {
        this.code = status.value();
        this.reason = status;
        if (message != null && message.length > 0) {
            this.message = message;
        }
    }
}