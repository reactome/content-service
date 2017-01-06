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
    public final String reason;
    public final String url;
    public String[] messages = null;

    public ErrorInfo(HttpStatus status, StringBuffer url, String... messages) {
        this.code = status.value();
        this.reason = status.name();
        this.url = url.toString();
        if (messages != null && messages.length > 0) {
            this.messages = messages;
        }
    }
}