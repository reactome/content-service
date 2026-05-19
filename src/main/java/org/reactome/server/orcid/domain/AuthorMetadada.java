package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class AuthorMetadada {

    @JsonProperty(value = "ORCID")
    private String orcid;

    @JsonProperty(value = "authenticated-orcid")
    private boolean authenticatedOrcid = true;

    private String given;

    private String family;

    private String sequence = "first";

    private String[] affiliation = {"ORCID, Inc."};

    public AuthorMetadada() {
    }

    public AuthorMetadada(String orcid, String name) {
        this.orcid = "https://orcid.org/" + orcid;
        String[] names = name.split(" ", 2);
        this.given = names[0];
        this.family = names[1];
    }

    public String getOrcid() {
        return orcid;
    }

    public boolean getAuthenticatedOrcid() {
        return authenticatedOrcid;
    }

    public String getGiven() {
        return given;
    }

    public String getFamily() {
        return family;
    }

    public String getSequence() {
        return sequence;
    }

    public String[] getAffiliation() {
        return affiliation;
    }
}
