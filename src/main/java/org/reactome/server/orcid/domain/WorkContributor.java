package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class WorkContributor {

    @JsonProperty("contributor-orcid")
    private SourceClientId contributorOrcid;

    @JsonProperty("credit-name")
    private Value creditName;

    @JsonProperty("contributor-email")
    private Value contributorEmail;

    @JsonProperty("contributor-attributes")
    private ContributorAttributes contributorAttributes;

    public WorkContributor() {
    }

    public WorkContributor(ContributorAttributes contributorAttributes) {
        this.contributorAttributes = contributorAttributes;
    }

    public WorkContributor(SourceClientId contributorOrcid, String creditName, String contributorEmail, ContributorAttributes contributorAttributes) {
        this.contributorOrcid = contributorOrcid;
        this.creditName = new Value(creditName);
        this.contributorEmail = new Value(contributorEmail);
        this.contributorAttributes = contributorAttributes;
    }

    public SourceClientId getContributorOrcid() {
        return contributorOrcid;
    }

    public void setContributorOrcid(SourceClientId contributorOrcid) {
        this.contributorOrcid = contributorOrcid;
    }

    public Value getCreditName() {
        return creditName;
    }

    public void setCreditName(Value creditName) {
        this.creditName = creditName;
    }

    public Value getContributorEmail() {
        return contributorEmail;
    }

    public void setContributorEmail(Value contributorEmail) {
        this.contributorEmail = contributorEmail;
    }

    public ContributorAttributes getContributorAttributes() {
        return contributorAttributes;
    }

    public void setContributorAttributes(ContributorAttributes contributorAttributes) {
        this.contributorAttributes = contributorAttributes;
    }

    @Override
    public String toString() {
        return "WorkContributor{" +
                "contributorOrcid=" + contributorOrcid +
                ", creditName=" + creditName +
                ", contributorEmail=" + contributorEmail +
                ", contributorAttributes=" + contributorAttributes +
                '}';
    }
}
