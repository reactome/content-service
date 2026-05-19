package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * It represents the works read from Orcid that were created by Reactome
 *
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
@JsonIgnoreProperties(value = {"external-ids"})
public class WorkGroup implements Serializable {

    @JsonProperty("last-modified-date")
    private Value lastModifiedDate;

    @JsonProperty("work-summary")
    private List<Work> works;

    public WorkGroup() {
    }

    public Value getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Value lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<Work> getWorks() {
        return works;
    }

    public void setWorks(List<Work> works) {
        this.works = works;
    }
}
