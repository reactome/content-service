package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Another JSON like is returned once works are submitted.
 * This POJO maps the json response
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class WorkResponse implements Serializable {

    @JsonProperty("work")
    private Work work;

    @JsonProperty("error")
    private ResponseError error;

    public WorkResponse() {
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }
}
