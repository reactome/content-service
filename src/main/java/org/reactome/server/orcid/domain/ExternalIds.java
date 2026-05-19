package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class ExternalIds implements Serializable {

    @JsonProperty("external-id")
    private List<ExternalId> externalId;

    public ExternalIds() {
    }

    public List<ExternalId> getExternalId() {
        if (externalId == null) externalId = new ArrayList<>();
        return externalId;
    }

    public void setExternalId(List<ExternalId> externalId) {
        this.externalId = externalId;
    }
}
