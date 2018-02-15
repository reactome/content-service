package org.reactome.server.service.exception;

public class DiagramExporterException extends RuntimeException {

    public DiagramExporterException(String message) {
        super(message);
    }

    public DiagramExporterException(String message, Throwable cause) {
        super(message, cause);
    }

}
