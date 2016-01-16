package org.reactome.server.tools.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactome.server.tools.exception.ContentServiceException;
import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
public class ContentServiceError {
    private int code;
    private String reason;
    private List<String> messages;

    public ContentServiceError(HttpStatus status) {
        this.code = status.value();
        this.reason = status.getReasonPhrase();
        this.messages = new LinkedList<>();
    }

    public ContentServiceError(HttpStatus status, String error) {
        this(status);
        this.add(error);
    }

    public ContentServiceError(HttpStatus status, List<String> messages) {
        this(status);
        this.addAll(messages);
    }

    public ContentServiceError(ContentServiceException ase){
        this(ase.getHttpStatus());
        this.addAll(ase.getErrorMessages());
    }

    public void add(String error){
        this.messages.add(error);
    }

    public void addAll(List<String> messages){
        this.messages.addAll(messages);
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public String toString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
