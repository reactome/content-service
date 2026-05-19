package org.reactome.server.orcid.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class Source implements Serializable {

    @JsonProperty("source-orcid")
    private String orcid;

    @JsonProperty("source-client-id")
    private SourceClientId clientId;

    @JsonProperty("source-name")
    private Value sourceName;

    public Source() {
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public SourceClientId getClientId() {
        return clientId;
    }

    public void setClientId(SourceClientId clientId) {
        this.clientId = clientId;
    }

    public Value getSourceName() {
        return sourceName;
    }

    public void setSourceName(Value sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public String toString() {
        return "Source{" +
                "orcid='" + orcid + '\'' +
                ", clientId=" + clientId +
                ", sourceName=" + sourceName +
                '}';
    }
}
