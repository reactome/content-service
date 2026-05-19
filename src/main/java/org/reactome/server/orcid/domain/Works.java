package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class Works implements Serializable {

    @JsonProperty("last-modified-date")
    private Value lastModifiedDate;

    @JsonProperty("group")
    private List<WorkGroup> group;

    @JsonProperty("path")
    private String path;

    public Works() {
    }

    public Value getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Value lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<WorkGroup> getGroup() {
        return group;
    }

    public void setGroup(List<WorkGroup> group) {
        this.group = group;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
