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
    public final String url;
    public final String[] message;


    public ErrorInfo(HttpStatus status, StringBuffer url, String... message) {
        this.status = status;
        this.url = url.toString();
        this.message = message;
    }
}