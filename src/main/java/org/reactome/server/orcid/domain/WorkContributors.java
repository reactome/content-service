package org.reactome.server.orcid.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class WorkContributors {

    @JsonProperty("contributor")
    private List<WorkContributor> contributors;

    public WorkContributors() {
    }

    public List<WorkContributor> getContributors() {
        if (contributors == null) contributors = new ArrayList<>();
        return contributors;
    }

    public void setContributors(List<WorkContributor> contributors) {
        this.contributors = contributors;
    }
}
