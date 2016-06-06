package org.reactome.server.service.exception.newExceptions;

import org.springframework.http.HttpStatus;

/**
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
public class ErrorInfo {

    public final HttpStatus status;
    public final String message;
    public final String url;
    public final Exception ex;

    public ErrorInfo(HttpStatus status, String message, StringBuffer url, Exception ex) {
        this.status = status;
        this.message = message;
        this.url = url.toString();
        this.ex = ex;
    }
}