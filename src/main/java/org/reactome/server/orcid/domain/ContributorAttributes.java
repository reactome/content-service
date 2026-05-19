package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class ContributorAttributes implements Serializable {

    @JsonProperty("contributor-sequence")
    private ContributorSequence sequence;

    @JsonProperty("contributor-role")
    private ContributorRole role;

    public ContributorAttributes() {
    }

    public ContributorAttributes(ContributorSequence sequence, ContributorRole role) {
        this.sequence = sequence;
        this.role = role;
    }

    public ContributorSequence getSequence() {
        return sequence;
    }

    public void setSequence(ContributorSequence sequence) {
        this.sequence = sequence;
    }

    public ContributorRole getRole() {
        return role;
    }

    public void setRole(ContributorRole role) {
        this.role = role;
    }

    public enum ContributorSequence {
        FIRST,
        ADDITIONAL
    }

    public enum ContributorRole {
        SUPPORT_STAFF,
        GRADUATE_STUDENT,
        PRINCIPAL_INVESTIGATOR,
        AUTHOR,
        CO_INVESTIGATOR,
        EDITOR,
        ASSIGNEE,
        CO_INVENTOR,
        CHAIR_OR_TRANSLATOR,
        POSTDOCTORAL_RESEARCHER,
        OTHER_INVENTOR
    }

    @Override
    public String toString() {
        return "ContributorAttributes{" +
                "sequence=" + sequence +
                ", role=" + role +
                '}';
    }
}
