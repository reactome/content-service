package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class WorkBulkResponse implements Serializable {

    @JsonProperty("bulk")
    private List<WorkResponse> bulk;

    @JsonIgnore
    private List<ResponseError> errors;

    @JsonIgnore
    private List<Work> works;

    public List<WorkResponse> getBulk() {
        if (bulk == null) bulk = new LinkedList<>();
        return bulk;
    }

    public void setBulk(List<WorkResponse> bulk) {
        this.bulk = bulk;
    }

    public List<ResponseError> getErrors() {
        if (errors == null) errors = new LinkedList<>();
        return errors;
    }

    public void setErrors(List<ResponseError> errors) {
        this.errors = errors;
    }
}
