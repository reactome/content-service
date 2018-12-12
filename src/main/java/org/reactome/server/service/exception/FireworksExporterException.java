package org.reactome.server.service.exception;

public class FireworksExporterException extends RuntimeException {

    public FireworksExporterException(String message) {
        super(message);
    }

    public FireworksExporterException(String message, Throwable cause) {
        super(message, cause);
    }

}
